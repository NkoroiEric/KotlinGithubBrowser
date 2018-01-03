package com.scaleup.kotlingithubbrowser.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.scaleup.kotlingithubbrowser.AppExecutors
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.api.RepoSearchResponse
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import com.scaleup.kotlingithubbrowser.util.AbsentLiveData
import com.scaleup.kotlingithubbrowser.util.RateLimiter
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import com.scaleup.kotlingithubbrowser.vo.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles Repo instances.
 *
 * unfortunate naming :/ .
 * Repo - value object name
 * Repository - type of this class.
 */
@Singleton
class RepoRepository @Inject constructor(instantAppExecutors: AppExecutors, db: GithubDb, dao: RepoDao, service: GithubService){
    private val appExecutors = instantAppExecutors
    private val githubDb = db
    private val repoDao = dao
    private val githubService = service
    private val repoListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepo(owner: String, name: String): LiveData<Resource<Repo>> {
        return object : NetworkBoundResource<Repo, Repo>(appExecutors){
            override fun saveCallResult(item: Repo?) {
                item?.let {  repoDao.insert(it) }
            }

            override fun shouldFetch(data: Repo?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<Repo> {
                return repoDao.load(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<Repo>> {
                return githubService.getRepo(owner,name)
            }

        }.asLiveData()
    }

    fun loadContributors(owner: String, name: String): LiveData<Resource<List<Contributor>>> {
        return object : NetworkBoundResource<List<Contributor>, List<Contributor>>(appExecutors){
            override fun saveCallResult(contributors: List<Contributor>?) {
                for (contributor in contributors!!) {
                    contributor.repoName = name
                    contributor.repoOwner = owner
                }
                githubDb.beginTransaction()
                try {
                    repoDao.createRepoIfNotExist(Repo(Repo.UNKNOWN_ID,
                            name, owner + "/" + name, "",
                            Repo.Owner(owner, null), 0))
                    repoDao.insertContributors(contributors)
                    githubDb.setTransactionSuccessful()
                } finally {
                    githubDb.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Contributor>?): Boolean {
                println("rece contributor list from githubDb: $data")
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<Contributor>> {
                return repoDao.loadContributors(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<List<Contributor>>> {
                return githubService.getContributors(owner, name)
            }

        }.asLiveData()
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(query, githubService, githubDb)
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.getLiveData()
    }

    fun search(query: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(appExecutors){
            override fun saveCallResult(item: RepoSearchResponse?) {
                val repoIds = item!!.repoIds
                val repoSearchResult = RepoSearchResult(
                        query, repoIds, item.total,item.nextPage)
                githubDb.beginTransaction()
                try {
                    repoDao.insertRepos(item.items)
                    repoDao.insert(repoSearchResult)
                    githubDb.setTransactionSuccessful()
                } finally {
                    githubDb.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return  data == null
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return Transformations.switchMap(repoDao.search(query), { searchData ->
                    if (searchData == null){
                        AbsentLiveData.create()
                    }else{
                        repoDao.loadOrdered(searchData.repoIds)
                    }
                })
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> {
                return githubService.searchRepos(query)
            }

            override fun processResponse(response: ApiResponse<RepoSearchResponse>): RepoSearchResponse{
                val body = response.body
                if (body == null){
                    body!!.nextPage = response.nextPage
                }
                return body
            }

        }.asLiveData()
    }

    fun loadRepos(login: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, List<Repo>>(appExecutors){
            override fun saveCallResult(item: List<Repo>?) {
                if (item != null){
                    repoDao.insertRepos(item.toMutableList())
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty() || repoListRateLimit.shouldFetch(login)
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return repoDao.loadRepositories(login)
            }

            override fun createCall(): LiveData<ApiResponse<List<Repo>>> {
                return  githubService.getRepos(login)
            }

            override fun onFetchFailed(){
                repoListRateLimit.reset(login)
            }

        }.asLiveData()
    }


}