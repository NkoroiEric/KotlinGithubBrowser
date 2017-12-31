package com.scaleup.kotlingithubbrowser.vo

import com.google.gson.annotations.SerializedName
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.support.annotation.NonNull


/**
 * Using name/owner_login as primary key instead of id since name/owner_login is always available
 * vs id is not.
 */
@Entity(indices = [(Index(value = ["id"],unique = true)), (Index("owner_login"))], primaryKeys = ["name", "owner_login" ])
data class Repo(
        val id: Int,
        @field:SerializedName("name")
        val name: String,
        @field:SerializedName("full_name")
        val fullName: String,
        @field:SerializedName("description")
        val description: String,
        @field:SerializedName("owner")
        @field:Embedded(prefix = "owner_")
        val owner: Owner,
        @field:SerializedName("stargazers_count")
        val stars: Int) {

    class Owner(
            @field:SerializedName("login")
            val login: String,
            @field:SerializedName("url")
            val url: String?) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Owner

            if (login != other.login) return false
            if (url != other.url) return false

            return true
        }

        override fun hashCode(): Int {
            var result = login.hashCode()
            result = 31 * result + (url?.hashCode() ?: 0)
            return result
        }
    }

    companion object {
        val UNKNOWN_ID = -1
    }
}
