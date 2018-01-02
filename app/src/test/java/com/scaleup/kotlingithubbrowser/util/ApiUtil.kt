package com.scaleup.kotlingithubbrowser.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import retrofit2.Response


object ApiUtil {
    fun <T> successCall(data: T): LiveData<ApiResponse<T>> {
        return createCall(Response.success(data))
    }

    fun <T> createCall(response: Response<T>): LiveData<ApiResponse<T>> {
        val data = MutableLiveData<ApiResponse<T>>()
        data.value = ApiResponse(response)
        return data
    }
}
