package com.scaleup.kotlingithubbrowser.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.scaleup.kotlingithubbrowser.GithubApp
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlin.properties.ReadWriteProperty

object AppInjector {

    fun init(app: GithubApp) {
        DaggerAppComponent.builder()
                .application(app)
                .build().inject(app)
       app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{
           override fun onActivityPaused(activity: Activity?) {

           }

           override fun onActivityResumed(activity: Activity?) {
           }

           override fun onActivityStarted(activity: Activity?) {
           }

           override fun onActivityDestroyed(activity: Activity?) {
           }

           override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
           }

           override fun onActivityStopped(activity: Activity?) {
           }

           override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                handleActivity(activity)
           }

       })
    }

    private fun handleActivity(activity: Activity?) {
        activity?.let {
            if (it is HasSupportFragmentInjector){
                AndroidInjection.inject(it)
            }else {
                (it as FragmentActivity).supportFragmentManager
                        .registerFragmentLifecycleCallbacks(object :
                        FragmentManager.FragmentLifecycleCallbacks(){
                            override fun onFragmentActivityCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
                                super.onFragmentActivityCreated(fm, f, savedInstanceState)
                                if (f is Injectable){
                                    AndroidSupportInjection.inject(f)
                                }
                            }
                        }, true)
            }
        }
    }
}