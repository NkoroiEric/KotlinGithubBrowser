package com.scaleup.kotlingithubbrowser.db

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.util.SparseIntArray
import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.RepoSearchResult
import java.util.Collections

@Dao
abstract class RepoDao {

    @Query("select * from RepoSearchResult where query = :query")
    abstract fun findSearchResult(query: String): RepoSearchResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(merged: RepoSearchResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(repo: Repo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRepos(items: MutableList<Repo>)

    @Query("select * from repo where owner_login = :login and name = :name")
    abstract fun load(login: String, name: String): LiveData<Repo>


    @Query("SELECT login, avatarUrl, repoName, repoOwner, contributions FROM contributor "
            + "WHERE repoName = :name AND repoOwner = :owner "
            + "ORDER BY contributions DESC")
    abstract fun loadContributors(owner: String, name: String): LiveData<List<Contributor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertContributors(contributors: List<Contributor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun createRepoIfNotExist(repo: Repo)

    @Query("select * from RepoSearchResult where query = :name")
    abstract fun search(name: String): LiveData<RepoSearchResult>



    fun loadOrdered(repoIds: List<Int>): LiveData<List<Repo>> {
        val order = mutableMapOf<Int,Int>()
        var index = 0
        for (repoId in repoIds) {
            order.put(repoId, index++)
        }
        return Transformations.map(loadById(repoIds)) { repositories ->
            Collections.sort(repositories) { r1, r2 ->
                val pos1 = order.get(r1.id)
                val pos2 = order.get(r2.id)
                pos1!! - pos2!!
            }
            repositories
        }
    }


    @Query("select * from repo where id in (:repoIds)")
    abstract fun loadById(repoIds: List<Int>): LiveData<List<Repo>>

}