package com.scaleup.kotlingithubbrowser.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.api.RepoSearchResponse
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import com.scaleup.kotlingithubbrowser.vo.Resource
import retrofit2.Response
import java.io.IOException

class FetchNextSearchPageTask(query: String, service: GithubService, db: GithubDb) : Runnable {
    private val liveData: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    private val query = query
    private val githubService = service
    private val db = db

    override fun run() {
        val current = db.repoDao().findSearchResult(query)
        if (current == null){
            liveData.postValue(null)
            return
        }
        val nextPage = current.next
        if (nextPage == null){
            liveData.postValue(Resource.success(false))
            return
        }
        try {
            val response = githubService
                    .searchRepos(query, nextPage).execute()
            val apiResponse = ApiResponse<RepoSearchResponse>(response)
            if (apiResponse.isSuccessFull){
                // we merge all repo ids into 1 list so that it is easier to fetch the result list.
                val ids = ArrayList<Int>()
                ids.addAll(current.repoIds)
                //noinspection ConstantConditions
                ids.addAll(apiResponse.body!!.repoIds)
                val merged = RepoSearchResult(query, ids,
                        apiResponse.body!!.total,apiResponse.nextPage)
                try {
                    db.apply {
                        beginTransaction()
                        repoDao().insert(merged)
                        repoDao().insertRepos(apiResponse.body!!.items)
                        setTransactionSuccessful()
                    }
                }finally {
                    db.endTransaction()
                }
                liveData.postValue(Resource.success(apiResponse.nextPage != null))
            } else {
                liveData.postValue(Resource.error(apiResponse.errorMessage!!, true))
            }
        }catch (e : IOException){
            liveData.postValue(Resource.error(e.message!!,true))
        }
    }

    fun getLiveData(): LiveData<Resource<Boolean>> {
        return liveData
    }

}