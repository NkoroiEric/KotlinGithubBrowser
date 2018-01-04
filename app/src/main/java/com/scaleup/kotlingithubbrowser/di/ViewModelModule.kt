package com.scaleup.kotlingithubbrowser.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.scaleup.kotlingithubbrowser.GithubViewModelFactory
import com.scaleup.kotlingithubbrowser.ui.repo.RepoViewModel
import com.scaleup.kotlingithubbrowser.ui.search.SearchViewModel
import com.scaleup.kotlingithubbrowser.ui.user.UserViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    abstract fun bindUserViewModel(userViewModel: UserViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RepoViewModel::class)
    abstract fun bindRepoViewModel(repoViewModel: RepoViewModel) : ViewModel

    @Binds
    abstract fun bindViewModelFactory(githubViewModelFactory: GithubViewModelFactory) : ViewModelProvider.Factory
}