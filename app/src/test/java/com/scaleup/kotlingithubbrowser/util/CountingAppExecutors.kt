package com.scaleup.kotlingithubbrowser.util

import com.scaleup.kotlingithubbrowser.AppExecutors
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class CountingAppExecutors {

    private val LOCK = java.lang.Object()

    private var taskCount = 0

    private val appExecutors : AppExecutors


    init {
        val increment = Runnable {
            synchronized(LOCK){
                taskCount--
                if (taskCount == 0){
                    LOCK.notifyAll()
                }
            }
        }

        val decrement = Runnable {
            synchronized(LOCK){
                taskCount++
            }
        }

        appExecutors = AppExecutors(
                CountingExecutors(increment, decrement),
                CountingExecutors(increment, decrement),
                CountingExecutors(increment, decrement))
    }

    fun getAppExecutors(): AppExecutors{
        return appExecutors
    }


    @Throws(InterruptedException::class, TimeoutException::class)
    fun drainTasks(time : Int, timeunit : TimeUnit) {
        val end = System.currentTimeMillis() + timeunit.toMillis(time.toLong())
        while (true){
            synchronized(LOCK){
                if (taskCount == 0){
                    return
                }
                val now = System.currentTimeMillis()
                val remaining = end - now
                if (remaining > 0){
                    LOCK.wait(remaining)
                }else{
                    throw TimeoutException("could not drain tasks")
                }
            }
        }
    }

    class CountingExecutors(inc: Runnable, dec: Runnable): Executor {

        private val delegate = Executors.newSingleThreadExecutor()

        private val increment : Runnable = inc
        private val decrement : Runnable = dec


        override fun execute(command: Runnable) {
            increment.run()
            delegate.execute {
                try {
                    command.run()
                } finally {
                    decrement.run()
                }
            }
        }
    }
}