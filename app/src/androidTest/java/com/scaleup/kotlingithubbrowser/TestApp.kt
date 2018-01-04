package com.scaleup.kotlingithubbrowser

import android.app.Application

/**
 * We use a separate App for tests to prevent initializing dependency injection.
 *
 * See {@link com.android.example.github.util.GithubTestRunner}.
 */
class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}