package com.scaleup.kotlingithubbrowser.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import com.scaleup.kotlingithubbrowser.vo.User

@Database(entities = [
Repo::class,
User::class,
Contributor::class,
RepoSearchResult::class], version = 1)
abstract class GithubDb : RoomDatabase() {
    abstract fun repoDao() : RepoDao
}