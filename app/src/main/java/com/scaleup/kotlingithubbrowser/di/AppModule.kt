package com.scaleup.kotlingithubbrowser.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides @Singleton
    fun provideContext(application: Application): Context{
        return application.applicationContext
    }

    @Provides @Singleton
    fun provideGithubDb(context: Context): GithubDb{
        return Room.databaseBuilder(context, GithubDb::class.java,"github.db").build()
    }

    @Provides @Singleton
    fun provideRepoDao(githubDb: GithubDb): RepoDao{
        return githubDb.repoDao()
    }
}