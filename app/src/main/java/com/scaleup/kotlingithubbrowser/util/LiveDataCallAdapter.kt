package com.scaleup.kotlingithubbrowser.util

import android.arch.lifecycle.LiveData
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A Retrofit adapterthat converts the Call into a LiveData of ApiResponse.
 * @param <R>
 */
class LiveDataCallAdapter<R>(responseType : Type) : CallAdapter<R, LiveData<ApiResponse<R>>>{
    private val responseType : Type = responseType

    override fun adapt(call: Call<R>?): LiveData<ApiResponse<R>> {
        return object : LiveData<ApiResponse<R>>(){
            val started = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)){
                    call?.enqueue(object : Callback<R>{
                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            postValue(ApiResponse(response))
                        }

                        override fun onFailure(call: Call<R>, t: Throwable) {
                            postValue(ApiResponse(t))
                        }

                    })
                }
            }
        }
    }

    override fun responseType(): Type = responseType


}