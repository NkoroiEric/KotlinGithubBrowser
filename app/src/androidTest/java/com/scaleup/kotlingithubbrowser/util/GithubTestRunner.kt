package com.scaleup.kotlingithubbrowser.util

import android.app.Application
import android.content.Context
import android.support.test.runner.AndroidJUnitRunner
import com.scaleup.kotlingithubbrowser.TestApp

/**
* Custom runner to disable dependency injection.
*/
class GithubTestRunner : AndroidJUnitRunner(){
    @Throws(InstantiationException::class, IllegalAccessException::class, ClassNotFoundException::class)
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestApp::class.simpleName, context)
    }
}