package com.scaleup.kotlingithubbrowser.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.scaleup.kotlingithubbrowser.api.ApiResponse

import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.db.UserDao
import com.scaleup.kotlingithubbrowser.util.ApiUtil
import com.scaleup.kotlingithubbrowser.util.InstantAppExecutors
import com.scaleup.kotlingithubbrowser.util.TestUtil
import com.scaleup.kotlingithubbrowser.vo.Resource
import com.scaleup.kotlingithubbrowser.vo.User
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class UserRepositoryTest {

    private lateinit var userDao : UserDao
    private lateinit var service : GithubService
    private lateinit var repo : UserRepository
    @Rule @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init(){
        userDao = mock(UserDao::class.java)
        service = mock(GithubService::class.java)
        repo = UserRepository(InstantAppExecutors(), userDao, service)
    }

    @Test
    fun loadUser(){
        repo.loadUser("abc")
        verify(userDao).findByLogin("abc")
    }

    @Test
    fun goToNetwork(){
        val dbData = MutableLiveData<User>()
        `when`(userDao.findByLogin("foo")).thenReturn(dbData)
        val user = TestUtil.createUser("foo")
        val call:LiveData<ApiResponse<User>> = ApiUtil.successCall(user)
        `when`(service.getUser("foo")).thenReturn(call)
        val observer = mock(Observer::class.java as Class<Observer<Resource< User>>>)

        repo.loadUser("foo").observeForever(observer)
        verify(service, never()).getUser("foo")
        val updateDbData = MutableLiveData<User>()
        `when`(userDao.findByLogin("foo")).thenReturn(updateDbData)
        dbData.value = null
        verify(service).getUser("foo")
    }

    @Test
    fun dontGoToNetwork(){
        val dbData = MutableLiveData<User>()
        val user = TestUtil.createUser("foo")
        dbData.value = user
        `when`(userDao.findByLogin("foo")).thenReturn(dbData)
        val observer = mock(Observer::class.java as Class<Observer<Resource< User>>>)

        repo.loadUser("foo").observeForever(observer)
        verify(service, never()).getUser("foo")
        verify(observer).onChanged(Resource.success(user))
    }
}