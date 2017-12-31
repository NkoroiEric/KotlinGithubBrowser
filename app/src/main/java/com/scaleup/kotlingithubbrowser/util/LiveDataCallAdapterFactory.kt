package com.scaleup.kotlingithubbrowser.util

import android.arch.lifecycle.LiveData
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type



class LiveDataCallAdapterFactory : CallAdapter.Factory(){

    @Throws(IllegalArgumentException::class)
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != LiveData::class.java){
            return null
        }
        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawObservable = getRawType(observableType)
        if (rawObservable != ApiResponse::class.java){
            throw IllegalArgumentException("type must be a resource")
        }
        if ((observableType !is ParameterizedType)){
            throw IllegalArgumentException("resource must be parameterised")
        }
        val bodyType = getParameterUpperBound(0, observableType)
        return LiveDataCallAdapter<Any>(bodyType)

    }

}
