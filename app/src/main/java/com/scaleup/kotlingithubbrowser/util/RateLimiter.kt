package com.scaleup.kotlingithubbrowser.util


import android.os.SystemClock
import java.util.concurrent.TimeUnit

/**
 * Utility class that decides whether we should fetch some data or not.
 */
class RateLimiter<KEY>(timeout:Int, timeUnit: TimeUnit) {
    private val timestamps : MutableMap<KEY, Long> = mutableMapOf()
    private val timeout = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shouldFetch(key: KEY): Boolean {
        val lastFetched = timestamps.get(key)
        val now = now()
        if (lastFetched == null){
            timestamps.put(key, now)
            return true
        }
        if (now - lastFetched  > timeout){
            timestamps.put(key, now)
            return true
        }
        return false
    }

    fun now(): Long {
        return SystemClock.uptimeMillis()
    }

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }
}