package com.scaleup.kotlingithubbrowser.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import com.google.gson.annotations.SerializedName
import android.arch.persistence.room.ForeignKey.CASCADE


@Entity( tableName = "contributor",
        primaryKeys = [ "repoName", "repoOwner", "login" ],
        foreignKeys = [(ForeignKey(entity = Repo::class,
        parentColumns = arrayOf("name", "owner_login"),
        childColumns = arrayOf("repoName", "repoOwner"),
        onUpdate = ForeignKey.CASCADE, deferred = true))])
data class Contributor(@field:SerializedName("login")
                       val login: String,
                       @field:SerializedName("contributions")
                       val contributions: Int,
                       @field:SerializedName("avatar_url")
                       val avatarUrl: String?) {


    lateinit var repoName: String

    lateinit var repoOwner: String
}
