package com.scaleup.kotlingithubbrowser.ui.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.support.annotation.VisibleForTesting
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.util.AbsentLiveData
import com.scaleup.kotlingithubbrowser.util.Objects
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.Resource
import javax.inject.Inject

class RepoViewModel @Inject constructor(repository: RepoRepository) : ViewModel() {

    @VisibleForTesting
    val repoId = MutableLiveData<RepoId>()

    @VisibleForTesting
    val repo: LiveData<Resource<Repo>> =
            Transformations.switchMap(repoId){ input ->
                if (input == null){
                    println("input is null")
                    AbsentLiveData.create()
                }else {
                    println("input is not null")
                    repository.loadRepo(input.owner, input.name)
                }
            }

    @VisibleForTesting
    val contributors : LiveData<Resource<List<Contributor>>> =
            Transformations.switchMap(repoId) { repoId ->
                if (repoId == null){
                    AbsentLiveData.create()
                }else {
                    repository.loadContributors(repoId.owner, repoId.name)
                }
            }

    @VisibleForTesting
    fun setId(owner: String?, name: String?){
        if (owner == null || name == null) return
        val update = RepoId(owner, name)
        if (Objects.equals(update, repoId.value)) return
        repoId.value = update
    }

    data class RepoId(val owner : String, val name : String)

    fun retry() {
        val current = repoId.value
        if (current != null){
            repoId.value = current
        }
    }
}