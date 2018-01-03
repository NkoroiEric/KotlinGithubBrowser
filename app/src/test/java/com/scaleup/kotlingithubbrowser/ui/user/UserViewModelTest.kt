package com.scaleup.kotlingithubbrowser.ui.user

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.repository.UserRepository
import com.scaleup.kotlingithubbrowser.vo.Resource
import com.scaleup.kotlingithubbrowser.vo.User
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*


@RunWith(JUnit4::class)
class UserViewModelTest {
    @Rule @JvmField val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var userViewModel : UserViewModel
    private lateinit var userRepository : UserRepository
    private lateinit var repoRepository : RepoRepository

    @Before
    fun setup(){
        userRepository = mock(UserRepository::class.java)
        repoRepository = mock(RepoRepository::class.java)
        userViewModel = UserViewModel(userRepository, repoRepository)
    }

    @Test
    fun testNull(){
        assertThat(userViewModel.getUser(), notNullValue())
        verify(userRepository, never()).loadUser(anyString())
        userViewModel.login.value = "foo"
        verify(userRepository, never()).loadUser(anyString())
    }

    @Test
    fun testCallRepo(){
        val captor = argumentCaptor<String>()
        userViewModel.getUser().observeForever(mock(Observer::class.java) as Observer<Resource<User>>)
        userViewModel.login.value = "abc"
        verify(userRepository).loadUser(captor.capture())
        assertThat(captor.firstValue, `is`("abc"))
        reset(userRepository)
        userViewModel.login.value = "ddd"
        verify(userRepository).loadUser(captor.capture())
        assertThat(captor.secondValue, `is`("ddd"))
    }

    @Test
    fun sendResultToUI(){
        val foo = MutableLiveData<Resource<User>>()
        val bar = MutableLiveData<Resource<User>>()
        `when`(userRepository.loadUser("foo")).thenReturn(foo)
        `when`(userRepository.loadUser("bar")).thenReturn(bar)
        val observer = mock(Observer::class.java)
        userViewModel.getUser().observeForever(observer as Observer<Resource<User>>)
        userViewModel.login.value = "foo"
        verify(observer, never()).onChanged(ArgumentMatchers.any(Resource::class.java) as Resource<User>?)
    }
}