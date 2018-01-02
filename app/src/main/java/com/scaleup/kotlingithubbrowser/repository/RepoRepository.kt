package com.scaleup.kotlingithubbrowser.repository

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.util.Log
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
    val appExecutors = instantAppExecutors
    val db = db
    val dao = dao
    val service = service
    private val repoListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepo(owner: String, name: String): LiveData<Resource<Repo>> {
        return object : NetworkBoundResource<Repo, Repo>(appExecutors){
            override fun saveCallResult(item: Repo?) {
                item?.let {  dao.insert(it) }
            }

            override fun shouldFetch(data: Repo?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<Repo> {
                return dao.load(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<Repo>> {
                return service.getRepo(owner,name)
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
                db.beginTransaction()
                try {
                    dao.createRepoIfNotExist(Repo(Repo.UNKNOWN_ID,
                            name, owner + "/" + name, "",
                            Repo.Owner(owner, null), 0))
                    dao.insertContributors(contributors)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Contributor>?): Boolean {
                println("rece contributor list from db: $data")
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<Contributor>> {
                return dao.loadContributors(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<List<Contributor>>> {
                return service.getContributors(owner, name)
            }

        }.asLiveData()
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(query, service,db)
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.getLiveData()
    }

    fun search(query: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(appExecutors){
            override fun saveCallResult(item: RepoSearchResponse?) {
                val repoIds = item!!.repoIds
                val repoSearchResult = RepoSearchResult(
                        query, repoIds, item.total,item.nextPage)
                db.beginTransaction()
                try {
                    dao.insertRepos(item.items)
                    dao.insert(repoSearchResult)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return  data == null
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return Transformations.switchMap(dao.search(query), { searchData ->
                    if (searchData == null){
                        AbsentLiveData.create()
                    }else{
                        dao.loadOrdered(searchData.repoIds)
                    }
                })
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> {
                return service.searchRepos(query)
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




}