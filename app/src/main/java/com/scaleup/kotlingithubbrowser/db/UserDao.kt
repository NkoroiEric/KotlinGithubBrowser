package com.scaleup.kotlingithubbrowser.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.scaleup.kotlingithubbrowser.vo.User

@Dao
interface UserDao {
    @Query("select * from user where login = :login")
    fun findByLogin(login: String):LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: User)

}