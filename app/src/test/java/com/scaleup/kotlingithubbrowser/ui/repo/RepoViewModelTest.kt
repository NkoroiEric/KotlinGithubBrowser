package com.scaleup.kotlingithubbrowser.ui.repo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.os.Looper
import com.nhaarman.mockito_kotlin.*
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.Resource
import org.junit.Rule
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.ArgumentMatchers.anyString
import java.util.*


@RunWith(JUnit4::class)
class RepoViewModelTest {

    @Rule @JvmField
    val instantTaskExecutor = InstantTaskExecutorRule()
    private lateinit var repository : RepoRepository
    private lateinit var repoViewModel : RepoViewModel
    private lateinit var lopper : Looper

    @Before
    fun init(){
        repository = mock()
        lopper = mock()
        repoViewModel = RepoViewModel(repository)
    }

    @Test
    fun testNotNull(){
        assertThat(repoViewModel.repo, `is`(notNullValue()))
        assertThat(repoViewModel.contributors, `is`(notNullValue()))
        verify(repository, never()).loadRepo(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun dontFetchWithoutObservers() {
        repoViewModel.repoId.value = RepoViewModel.RepoId("a", "b")
        verify(repository, never()).loadRepo(anyString(), anyString())
    }

    @Test
    fun fetchWhenObserved() {
        val owner = argumentCaptor<String>()
        val name = argumentCaptor<String>()

        repoViewModel.setId("a", "b")
        val observer: Observer<Resource<Repo>> = mock()
        repoViewModel.repo.observeForever( observer )
        verify(repository, times(1)).loadRepo(owner.capture(),
                name.capture())
        assertThat(owner.firstValue, `is`("a"))
        assertThat(name.firstValue, `is`("b"))
    }

    @Test
    fun changeWhileObserved() {
        val owner = argumentCaptor<String>()
        val name = argumentCaptor<String>()
        val observer :Observer<Resource<Repo>> = mock()
        repoViewModel.repo.observeForever(observer)

        repoViewModel.setId("a", "b")
        repoViewModel.setId("c", "d")

        verify(repository, times(2)).loadRepo(owner.capture(),
                name.capture())
        assertThat(owner.allValues, `is`(Arrays.asList("a", "c")))
        assertThat(name.allValues, `is`(Arrays.asList("b", "d")))
    }

    @Test
    fun contributors() {
        val observer :Observer<Resource<List<Contributor>>> = mock()
        repoViewModel.contributors.observeForever(observer)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verify(repository).loadContributors("foo", "bar")
    }

    @Test
    fun resetId() {
        val observer:Observer<RepoViewModel.RepoId> = mock()
        repoViewModel.repoId.observeForever(observer)
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("foo", "bar")
        verify(observer).onChanged(RepoViewModel.RepoId("foo", "bar"))
        reset(observer)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("a", "b")
        verify(observer).onChanged(RepoViewModel.RepoId("a", "b"))
    }

    @Test
    fun retry() {
        repoViewModel.retry()
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(repository)
        val observer: Observer<Resource<Repo>> = mock()
        repoViewModel.repo.observeForever(observer)
        verify(repository).loadRepo("foo", "bar")
        reset(repository)
        repoViewModel.retry()
        verify(repository).loadRepo("foo", "bar")
    }



}