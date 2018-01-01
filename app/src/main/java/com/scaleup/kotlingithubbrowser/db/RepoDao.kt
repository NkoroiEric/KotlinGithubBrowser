package com.scaleup.kotlingithubbrowser.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult

@Dao
interface RepoDao {
    @Query("select * from RepoSearchResult where query = :name")
    fun search(name: String): LiveData<RepoSearchResult>

    @Query("select * from RepoSearchResult where query = :query")
    fun findSearchResult(query: String): RepoSearchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(merged: RepoSearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRepos(items: MutableList<Repo>)
}