package com.scaleup.kotlingithubbrowser.repository


import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.api.RepoSearchResponse
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import com.scaleup.kotlingithubbrowser.util.TestUtil
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import com.scaleup.kotlingithubbrowser.vo.Resource
import okhttp3.Headers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*

@RunWith(JUnit4::class)
class FetchNextSearchPageTaskTest {
    @Rule @JvmField val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var service : GithubService
    private lateinit var db : GithubDb
    private lateinit var repoDao : RepoDao
    private lateinit var task : FetchNextSearchPageTask
    private lateinit var value : LiveData<Resource<Boolean>>
    private lateinit var observer : Observer<Resource<Boolean>>

    @Before fun init(){
        service = mock(GithubService::class.java)
        db = mock(GithubDb::class.java)
        repoDao = mock(RepoDao::class.java)
        `when`(db.repoDao()).thenReturn(repoDao)
        task = FetchNextSearchPageTask("foo", service, db)
        observer = mock(Observer::class.java as Class<Observer<Resource<Boolean>>>)
        task.getLiveData().observeForever(observer)
    }

    @Test
    fun  withoutResult(){
        `when`(repoDao.search("foo")).thenReturn(null)
        task.run()
        verify(observer).onChanged(null)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }

    @Test 
    fun noNextPage(){
        createDbResult(null)
        task.run()
        verify(observer).onChanged(Resource.success(false))
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }
    private val result = RepoSearchResponse()
    @Test
    fun nextPageWithNull(){
        createDbResult(1)
        result.total = 10
        val repos = TestUtil.createRepos(10, "a", "b","c")
        result.items = repos.toMutableList()
        val call = createCall(result, null)
        `when`(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify(repoDao).insertRepos(repos.toMutableList())
        verify(observer).onChanged(Resource.success(false))
    }

    @Test
    fun nextPageWithMore(){
        createDbResult(1)
        result.total = 10
        val repos = TestUtil.createRepos(10, "a", "b", "c")
        result.items = repos.toMutableList()
        result.nextPage = 2
        val call = createCall(result, 2)
        `when`(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify(repoDao).insertRepos(repos.toMutableList())
        verify(observer).onChanged(Resource.success(true))
    }

    @Test
    fun nextPageIOError(){
        createDbResult(1)
        val call: Call<RepoSearchResponse> = mock(Call::class.java as Class<Call<RepoSearchResponse>>)
        `when`(call.execute()).thenThrow(IOException("bar"))
        `when`(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify(observer).onChanged(Resource.error("bar", true))
    }

    private fun createDbResult(nextPage: Int?) {
        val result = RepoSearchResult("foo", Collections.emptyList(),
                0, nextPage)
        `when`(repoDao.findSearchResult("foo")).thenReturn(result)
    }

    @Throws(IOException::class)
    private fun createCall(body : RepoSearchResponse, nextPage: Int?): Call<RepoSearchResponse>? {
        val headers =  if (nextPage == null) null else Headers
                .of("link",
                        "<https://api.github.com/search/repositories?q=foo&page=" + nextPage
                                + ">; rel=\"next\"")
        val success = if (headers == null) Response.success(body) else
            Response.success(body,headers)
        val call = mock(Call::class.java)
        `when`(call.execute()).thenReturn(success)
        return call as Call<RepoSearchResponse>?
    }

}