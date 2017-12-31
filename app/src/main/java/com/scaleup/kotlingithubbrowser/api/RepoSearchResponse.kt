package com.scaleup.kotlingithubbrowser.api

import com.google.gson.annotations.SerializedName
import com.scaleup.kotlingithubbrowser.vo.Repo


/**
 * POJO to hold repo search responses. This is different from the Entity in the database because
 * we are keeping a search result in 1 row and denormalizing list of results into a single column.
 */
class RepoSearchResponse {
    @SerializedName("total_count")
    var total: Int = 0
    @SerializedName("items")
    var items: MutableList<Repo> = mutableListOf()
    var nextPage: Int? = null

    val repoIds: List<Int>
        get() {
            val repoIds = mutableListOf<Int>()
            for (i in items) {
                repoIds.add(i.id)
            }
            return repoIds
        }
}
