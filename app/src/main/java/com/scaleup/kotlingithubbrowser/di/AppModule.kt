package com.scaleup.kotlingithubbrowser.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import com.scaleup.kotlingithubbrowser.db.UserDao
import com.scaleup.kotlingithubbrowser.util.LiveDataCallAdapter
import com.scaleup.kotlingithubbrowser.util.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule {

    @Provides @Singleton
    fun provideGithubService() : GithubService{
        return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(GithubService::class.java)
    }

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

    @Provides @Singleton
    fun provideUserDao(githubDb: GithubDb): UserDao{
        return githubDb.userDao()
    }
}