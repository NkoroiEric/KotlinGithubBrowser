package com.scaleup.kotlingithubbrowser.di

import com.scaleup.kotlingithubbrowser.ui.repo.RepoFragment
import com.scaleup.kotlingithubbrowser.ui.search.SearchFragment
import com.scaleup.kotlingithubbrowser.ui.user.UserFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModel {
    @ContributesAndroidInjector
    abstract fun contributeSearchFragment() : SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment() : UserFragment

    @ContributesAndroidInjector
    abstract fun contributeRepoFragment() : RepoFragment
}