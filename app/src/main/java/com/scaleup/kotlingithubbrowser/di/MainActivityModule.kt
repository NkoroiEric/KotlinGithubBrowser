package com.scaleup.kotlingithubbrowser.di

import com.scaleup.kotlingithubbrowser.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuilderModel::class])
    abstract fun contributeMainActivity() : MainActivity
}