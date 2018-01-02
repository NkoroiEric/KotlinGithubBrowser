package com.scaleup.kotlingithubbrowser.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import com.scaleup.kotlingithubbrowser.AppExecutors
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.db.UserDao
import com.scaleup.kotlingithubbrowser.vo.Resource
import com.scaleup.kotlingithubbrowser.vo.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(instantExecutorRule: AppExecutors, userDao: UserDao, service: GithubService) {
    val appExecutors = instantExecutorRule
    val userDao = userDao
    val service = service

    fun loadUser(query: String) :LiveData<Resource<User>> {
        return object :NetworkBoundResource<User, User>(appExecutors){
            override fun saveCallResult(item: User?) {
                if (item != null){
                    userDao.insert(item)
                }
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<User> {
                return userDao.findByLogin(query)
            }

            override fun createCall(): LiveData<ApiResponse<User>> {
                return service.getUser(query)
            }

        }.asLiveData()
    }

}