package com.scaleup.kotlingithubbrowser.ui.user

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.support.annotation.VisibleForTesting
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.repository.UserRepository
import com.scaleup.kotlingithubbrowser.util.AbsentLiveData
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.Resource
import com.scaleup.kotlingithubbrowser.vo.User
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

class UserViewModel @Inject constructor(userRepository: UserRepository, repoRepository: RepoRepository) : ViewModel() {

    private val user: LiveData<Resource<User>>
    @VisibleForTesting
    var login = MutableLiveData<String>()
        set(value) {
            if (value == field){
                return
            }
            field = value
        }
    private val repositories: LiveData<Resource<List<Repo>>>

    init {
        user = Transformations.switchMap(login, Function { login ->
            if (login == null){
                AbsentLiveData.create()
            }else{
                userRepository.loadUser(login)
            }
        })
        repositories = Transformations.switchMap(login, { login ->
            if (login == null){
                AbsentLiveData.create()
            }else{
                repoRepository.loadRepos(login)
            }
        })
    }



    @VisibleForTesting
    fun getUser(): LiveData<Resource<User>> {
        return user
    }

    @VisibleForTesting
    fun getRepositories(): LiveData<Resource<List<Repo>>> {
        return repositories
    }

    @VisibleForTesting
    fun retry(){
        if (login.value != null){
            login.setValue(login.value)
        }
    }

}