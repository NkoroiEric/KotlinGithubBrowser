package com.scaleup.kotlingithubbrowser

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.scaleup.kotlingithubbrowser.ui.common.NavigationController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchAndroidInjector : DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var navigationController : NavigationController

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
            dispatchAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null){
            navigationController.navigateToSearch()
        }
    }
}
