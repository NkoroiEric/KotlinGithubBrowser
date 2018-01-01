package com.scaleup.kotlingithubbrowser.vo


import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import com.scaleup.kotlingithubbrowser.db.GithubTypeConverters

@Entity(primaryKeys = ["query"])
@TypeConverters(GithubTypeConverters::class)
data class RepoSearchResult(val query : String,
                            val repoIds : List<Int>,
                            val totalCount : Int,
                            val next : Int?)