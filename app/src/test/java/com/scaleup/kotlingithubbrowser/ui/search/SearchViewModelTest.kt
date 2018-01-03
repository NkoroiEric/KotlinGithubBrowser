package com.scaleup.kotlingithubbrowser.ui.search

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.*
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.Resource
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SearchViewModelTest {
    @Rule @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var searchViewModel : SearchViewModel
    private lateinit var repository : RepoRepository
    private val result :Observer<Resource<List<Repo>>> = mock()
    @Before
    fun init(){
        repository = mock()
        searchViewModel = SearchViewModel(repository)
    }

    @Test
    fun empty(){
        searchViewModel.results.observeForever(result)
        searchViewModel.loadNextPage()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun basic(){
        searchViewModel.results.observeForever(result)
        searchViewModel.setQuery("foo")
        verify(repository).search("foo")
        verify(repository, never()).searchNextPage("foo")
    }

    @Test
    fun noObserverNoQuery(){
        whenever(repository.searchNextPage("foo")).thenReturn(mock())
        searchViewModel.setQuery("foo")
        verify(repository, never()).search("foo")
        // next page is user interaction and even if loading state is
        // not observed, we query
        // would be better to avoid that if main search query is not observed
        searchViewModel.loadNextPage()
        verify(repository).searchNextPage("foo")
    }

    @Test
    fun swap(){
        val nextPage = MutableLiveData<Resource<Boolean>>()
        whenever(repository.searchNextPage("foo")).thenReturn(nextPage)

        val result:Observer<Resource<List<Repo>>> = mock()
        searchViewModel.results.observeForever(result)
        verifyNoMoreInteractions(repository)
        searchViewModel.setQuery("foo")
        verify(repository).search("foo")
        searchViewModel.loadNextPage()

        searchViewModel.loadMoreStatus().observeForever(mock())
        verify(repository).searchNextPage("foo")
        assertThat(nextPage.hasActiveObservers(),  `is`(true))
        searchViewModel.setQuery("bar")
        assertThat(nextPage.hasActiveObservers(), `is`(false))
        verify(repository).search("bar")
        verify(repository, never()).searchNextPage("bar")
    }

    @Test
    fun refresh(){
        searchViewModel.refresh()
        verifyNoMoreInteractions(repository)
        searchViewModel.setQuery("foo")
        searchViewModel.refresh()
        verifyNoMoreInteractions(repository)
        searchViewModel.results.observeForever(mock())
        verify(repository).search("foo")
        reset(repository)
        searchViewModel.refresh()
        verify(repository).search("foo")
    }

    @Test
    fun resetSameQuery(){
        searchViewModel.results.observeForever(mock())
        searchViewModel.setQuery("foo")
        verify(repository).search("foo")
        reset(repository)
        searchViewModel.setQuery("FOO")
        verifyNoMoreInteractions(repository)
        searchViewModel.setQuery("bar")
        verify(repository).search("bar")
    }
}