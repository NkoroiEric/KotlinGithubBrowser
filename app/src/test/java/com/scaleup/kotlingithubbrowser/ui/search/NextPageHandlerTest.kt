package com.scaleup.kotlingithubbrowser.ui.search

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.vo.Resource
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`



@RunWith(JUnit4::class)
class NextPageHandlerTest{
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var pageHandler : SearchViewModel.NextPageHandler
    private lateinit var repository : RepoRepository

    @Before
    fun init(){
        repository = mock()
        pageHandler = SearchViewModel.NextPageHandler(repository)
    }

    @Test
    fun constructor(){
        val initial:SearchViewModel.LoadMoreState  = getStatus()
        assertThat(initial, notNullValue())
        assertThat(initial.isRunning, `is`(false))
        assertThat(initial.getErrorMessageIfNotHandled(), notNullValue())
    }

    @Test
    fun reloadSameValue(){
        enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        verify(repository).searchNextPage("foo")

        reset(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)
    }

    @Test fun success(){
        val liveData = enqueueResponse("foo")

        pageHandler.queryNextPage("foo")
        verify(repository).searchNextPage("foo")
        assertThat(liveData.hasActiveObservers(), `is`(true))
        pageHandler.onChanged(Resource.loading(null))
        assertThat(liveData.hasActiveObservers(), `is`(true))
        assertThat(getStatus().isRunning, `is`(true))

        pageHandler.onChanged(Resource.success(true))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat(pageHandler.hasMore, `is`(true))
        assertThat(getStatus().isRunning, `is`(false))
        assertThat(liveData.hasActiveObservers(), `is`(false))

        //requery
        reset(repository)
        val nextPage = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        verify(repository).searchNextPage("foo")
        assertThat(nextPage.hasActiveObservers(), `is`(true))

        pageHandler.onChanged(Resource.success(false))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat(pageHandler.hasMore, `is`(false))
        assertThat(getStatus().isRunning, `is`(false))
        assertThat(nextPage.hasActiveObservers(), `is`(false))

        //retry no query
        reset(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)

        //query another
        val bar = enqueueResponse("bar")
        pageHandler.queryNextPage("bar")
        verify(repository).searchNextPage("bar")
        assertThat(bar.hasActiveObservers(), `is`(true))
    }


    @Test
    fun failure(){
        val liveData = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        assertThat(liveData.hasActiveObservers(), `is`(true))
        pageHandler.onChanged(Resource.error("idk", false))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat(getStatus().errorMessage, `is`("idk"))
        assertThat(getStatus().getErrorMessageIfNotHandled(), `is`("idk"))
        assertThat(getStatus().getErrorMessageIfNotHandled(), `is`(""))
        assertThat(getStatus().isRunning, `is`(false))
        assertThat(pageHandler.hasMore, `is`(true))

        reset(repository)

        val liveData2 = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        assertThat(liveData2.hasActiveObservers(), `is`(true))
        assertThat(getStatus().isRunning, `is`(true))
        pageHandler.onChanged(Resource.success(false))
        assertThat(getStatus().isRunning, `is`(false))
        assertThat(getStatus().getErrorMessageIfNotHandled(), `is`("error message is empty"))
        assertThat(pageHandler.hasMore, `is`(false))
    }

    private fun enqueueResponse(query: String): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        `when`(repository.searchNextPage(query)).thenReturn(liveData)
        return liveData
    }

    private fun getStatus(): SearchViewModel.LoadMoreState {
        return pageHandler.loadMoreState.value!!
    }

}