package com.scaleup.kotlingithubbrowser.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.api.RepoSearchResponse
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import com.scaleup.kotlingithubbrowser.util.AbsentLiveData
import com.scaleup.kotlingithubbrowser.util.ApiUtil.successCall
import com.scaleup.kotlingithubbrowser.util.InstantAppExecutors
import com.scaleup.kotlingithubbrowser.util.TestUtil
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import com.scaleup.kotlingithubbrowser.vo.Resource
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import retrofit2.Response
import java.io.IOException
import java.util.*

@RunWith(JUnit4::class)
class RepoRepositoryTest {
    private lateinit var repository : RepoRepository
    private lateinit var dao : RepoDao
    private lateinit var service : GithubService
    @Rule @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Before
    fun init(){
        dao = mock(RepoDao::class.java)
        service = mock(GithubService::class.java)
        val db = mock(GithubDb::class.java)
        `when`(db.repoDao()).thenReturn(dao)
        repository = RepoRepository(InstantAppExecutors(), db, dao, service)
    }

    @Test
    fun loadRepoFromNetwork(){
        val dbData = MutableLiveData<Repo>()
        `when`(dao.load("foo", "bar")).thenReturn(dbData)

        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val call = successCall(repo)
        `when`(service.getRepo("foo", "bar")).thenReturn(call)

        val data:LiveData<Resource<Repo>> = repository.loadRepo("foo", "bar")
        verify(dao).load("foo", "bar")
        verifyNoMoreInteractions(service)

        val observer = mock(Observer::class.java)
        data.observeForever(observer as Observer<Resource<Repo>>)
        verifyNoMoreInteractions(service)
        verify(observer).onChanged(Resource.loading(null))
        val updatedData = MutableLiveData<Repo>()
        `when`(dao.load("foo", "bar")).thenReturn(updatedData)

        dbData.postValue(null)
        verify(service).getRepo("foo", "bar")
        verify(dao).insert(repo)

        updatedData.postValue(repo)
        verify(observer).onChanged(Resource.success(repo))
    }

    @Test
    @Throws(IOException::class)
    fun loadContributors() {
        val dbData = MutableLiveData<List<Contributor>>()
        `when`(dao.loadContributors("foo", "bar")).thenReturn(dbData)

        val data = repository.loadContributors("foo",
                "bar")
        verify<RepoDao>(dao).loadContributors("foo", "bar")

        verify(service, never()).getContributors(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributor = TestUtil.createContributor(repo, "log", 3)
        // network does not send these
        contributor.repoOwner = ""
        contributor.repoName = ""
        val contributors = listOf(contributor)
        val call = successCall(contributors)
        `when`(service.getContributors("foo", "bar"))
                .thenReturn(call)

        val observer = mock(Observer::class.java)
        data.observeForever(observer as Observer<Resource<List<Contributor>>>)

        verify<Observer<Resource<List<Contributor>>>>(observer).onChanged(Resource.loading(null))

        val updatedDbData = MutableLiveData<List<Contributor>>()
        `when`(dao.loadContributors("foo", "bar")).thenReturn(updatedDbData)
        dbData.value = emptyList()

        verify<GithubService>(service).getContributors("foo", "bar")
        val inserted = argumentCaptor<List<Contributor>>()
        verify<RepoDao>(dao).insertContributors(inserted.capture())


        assertThat(inserted.firstValue.size, `is`(1))
        val first = inserted.firstValue.get(0)
        assertThat(first.repoName, `is`("bar"))
        assertThat(first.repoOwner, `is`("foo"))

        updatedDbData.setValue(contributors)
        verify<Observer<Resource<List<Contributor>>>>(observer).onChanged(Resource.success(contributors))
    }

    @Test
    fun searchNextPage_null(){
        `when`(dao.findSearchResult("foo")).thenReturn(null)
        val observer = mock(Observer::class.java as  Class<Observer<Resource<Boolean>>>)
        repository.searchNextPage("foo").observeForever(observer)
        verify(observer).onChanged(null)
    }


    @Test
    fun search_fromDb() {
        val ids = Arrays.asList(1, 2)

        val observer = mock(Observer::class.java)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        val repositories = MutableLiveData<List<Repo>>()

        `when`(dao!!.search("foo")).thenReturn(dbSearchResult)

        repository.search("foo").observeForever(observer as Observer<Resource<List<Repo>>>)

        verify<Observer<Resource<List<Repo>>>>(observer).onChanged(Resource.loading(null))
        verifyNoMoreInteractions(service)
        reset<Observer<Resource<List<Repo>>>>(observer)

        val dbResult = RepoSearchResult("foo", ids, 2, null)
        `when`(dao.loadOrdered(ids)).thenReturn(repositories)

        dbSearchResult.postValue(dbResult)

        val repoList = ArrayList<Repo>()
        repositories.postValue(repoList)
        verify<Observer<Resource<List<Repo>>>>(observer).onChanged(Resource.success(repoList))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun search_fromServer() {
        val ids = Arrays.asList(1, 2)
        val repo1 = TestUtil.createRepo(1, "owner", "repo 1", "desc 1")
        val repo2 = TestUtil.createRepo(2, "owner", "repo 2", "desc 2")

        val observer = mock(Observer::class.java)
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        val repositories = MutableLiveData<List<Repo>>()

        val apiResponse = RepoSearchResponse()
        val repoList = Arrays.asList(repo1, repo2)
        apiResponse.items = repoList
        apiResponse.total = 2

        val callLiveData = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        `when`(service.searchRepos("foo")).thenReturn(callLiveData)

        `when`(dao.search("foo")).thenReturn(dbSearchResult)

        repository.search("foo").observeForever(observer as Observer<Resource<List<Repo>>>)

        verify<Observer<Resource<List<Repo>>>>(observer).onChanged(Resource.loading(null))
        verifyNoMoreInteractions(service)
        reset<Observer<Resource<List<Repo>>>>(observer)

        `when`(dao.loadOrdered(ids)).thenReturn(repositories)
        dbSearchResult.postValue(null)
        verify<RepoDao>(dao, never()).loadOrdered(emptyList())

        verify<GithubService>(service).searchRepos("foo")
        val updatedResult = MutableLiveData<RepoSearchResult>()
        `when`(dao.search("foo")).thenReturn(updatedResult)
        updatedResult.postValue(RepoSearchResult("foo", ids, 2, null))

        callLiveData.postValue(ApiResponse(Response.success(apiResponse)))
        verify<RepoDao>(dao).insertRepos(repoList)
        repositories.postValue(repoList)
        verify<Observer<Resource<List<Repo>>>>(observer).onChanged(Resource.success(repoList))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun search_fromServer_error(){
        `when`(dao.search("foo")).thenReturn(AbsentLiveData.create())
        val apiResponse = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        `when`(service.searchRepos("foo")).thenReturn(apiResponse)

        val observer = mock(Observer::class.java)
        repository.search("foo").observeForever(observer as Observer<Resource<List<Repo>>>)
        verify(observer).onChanged(Resource.loading(null))

        apiResponse.postValue(ApiResponse(Exception("idk")))
        verify(observer).onChanged(Resource.error("idk", null))
    }



}