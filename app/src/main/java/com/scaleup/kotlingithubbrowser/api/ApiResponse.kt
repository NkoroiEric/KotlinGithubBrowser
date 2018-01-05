package com.scaleup.kotlingithubbrowser.api

import android.util.ArrayMap
import android.util.Log
import java.util.Collections.emptyMap
import org.junit.experimental.results.ResultMatchers.isSuccessful
import retrofit2.Response
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
/**
 * Common class used by API responses.
 * @param <T>
</T> */
const val KOTLIN_GITHUB: String = "KOTLIN_GITHUB"
class ApiResponse<T>  {
    val links: MutableMap<String, String> = mutableMapOf()
    var body: T? = null
    var code: Int = 0
    var errorMessage: String? = null
    val isSuccessFull: Boolean
        get() {
            return code <= 200 && code < 300
        }
    val nextPage: Int?
        get() {
            val next = links.get(NEXT_LINK) ?: return null
            val matcher = PAGE_PATTERN.matcher(next)
            if (!matcher.find() || matcher.groupCount() != 1) return null
            try {
                return Integer.parseInt(matcher.group(1))
            }catch (e : NumberFormatException){
                Log.d(KOTLIN_GITHUB,"Cannot parse next page from $next")
                return null
            }
        }
    constructor(error : Throwable){
        code = 500
        body = null
        errorMessage = error.message
    }
    constructor(response: Response<T>){
        code = response.code()
        if (response.isSuccessful){
            body = response.body()
            println(body)
            errorMessage = null
        }else {
            body = null
            try {
                if (response.errorBody() != null){
                    errorMessage = response.errorBody()!!.string()
                }else{
                    errorMessage = response.message()
                }
            }catch (ignored : IOException){

            }
        }
        val linkHeader = response.headers().get("link")
        if (linkHeader != null) {
            links.clear()
            val matcher = LINK_PATTERN.matcher(linkHeader)
            while (matcher.find()) {
                val count = matcher.groupCount()
                if (count == 2) {
                    links.put(matcher.group(2), matcher.group(1))
                }
            }
        }
    }


    companion object {
        private val LINK_PATTERN = Pattern
                       .compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private val PAGE_PATTERN = Pattern.compile("\\bpage=(\\d+)")
        private val NEXT_LINK = "next"
    }
}
