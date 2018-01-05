package com.scaleup.kotlingithubbrowser

import android.app.Activity
import android.app.Application
import com.scaleup.kotlingithubbrowser.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class GithubApp : Application(), HasActivityInjector{

    @Inject
    lateinit var dispatch : DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity>  = dispatch

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }
}