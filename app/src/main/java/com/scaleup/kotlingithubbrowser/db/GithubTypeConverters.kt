package com.scaleup.kotlingithubbrowser.db

import android.annotation.SuppressLint
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.util.StringUtil
import java.util.*


object GithubTypeConverters {

    @SuppressLint("RestrictedApi")
    @TypeConverter @JvmStatic
    fun stringToIntList(data: String?): List<Int> {
        return if (data == null) {
            Collections.emptyList()
        } else
            StringUtil.splitToIntList(data)!!
    }

    @SuppressLint("RestrictedApi")
    @TypeConverter @JvmStatic
    fun intListToString(ints: List<Int>): String {
        return StringUtil.joinIntoString(ints)!!
    }
}
