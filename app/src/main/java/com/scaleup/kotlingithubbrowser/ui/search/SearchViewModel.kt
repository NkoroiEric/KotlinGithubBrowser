package com.scaleup.kotlingithubbrowser.ui.search

import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import android.support.annotation.VisibleForTesting
import com.scaleup.kotlingithubbrowser.repository.RepoRepository
import com.scaleup.kotlingithubbrowser.util.Objects
import com.scaleup.kotlingithubbrowser.vo.Resource
import com.scaleup.kotlingithubbrowser.vo.Status
import javax.inject.Inject
import com.scaleup.kotlingithubbrowser.ui.search.SearchViewModel.LoadMoreState
import android.databinding.adapters.NumberPickerBindingAdapter.setValue
import com.scaleup.kotlingithubbrowser.util.AbsentLiveData
import com.scaleup.kotlingithubbrowser.vo.Repo
import java.util.*


class SearchViewModel @Inject constructor(repoRepository: RepoRepository): ViewModel() {

    private val query = MutableLiveData<String>()
    private val nextPageHandler : NextPageHandler = NextPageHandler(repoRepository)
    @VisibleForTesting
    internal val results : LiveData<Resource<List<Repo>>> =
            Transformations.switchMap(query) { input ->
                if (input == null || input.trim().isEmpty()){
                    AbsentLiveData.create()
                }else {
                    repoRepository.search(input)
                }
            }

    fun setQuery(originalInput : String){
        val input = originalInput.toLowerCase(Locale.getDefault()).trim()
        if (Objects.equals(input, query.value)) return
        nextPageHandler.reset()
        query.value = input
    }

    @VisibleForTesting
    fun loadNextPage(){
        val value = query.value
        if (value == null || value.trim().isEmpty()) return
        nextPageHandler.queryNextPage(value)
    }

    @VisibleForTesting
    fun loadMoreStatus(): LiveData<LoadMoreState> {
        return nextPageHandler.loadMoreState
    }

    fun refresh(){
        if (query.value != null) query.value = query.value
    }

    class LoadMoreState(running: Boolean, errorM: String?){
        internal val errorMessage = errorM
        private var handledError = false
        val isRunning = running



        fun getErrorMessageIfNotHandled(): String {
            if (handledError) return ""
            handledError = true
            return errorMessage?:"error message is empty"
        }
    }

    @VisibleForTesting
    class NextPageHandler(repoRepository: RepoRepository) : Observer<Resource<Boolean>>{
        private var nextPageLiveData : LiveData<Resource<Boolean>>? = null
        @VisibleForTesting
        internal val loadMoreState = MutableLiveData<LoadMoreState>()
        private var query : String? = null
        private val repository: RepoRepository = repoRepository
        @VisibleForTesting
        var hasMore : Boolean = false

        init {
            reset()
        }

        fun queryNextPage(query : String){
            if (Objects.equals(this.query, query)) return
            unregister()
            this.query = query
            nextPageLiveData = repository.searchNextPage(query)
            loadMoreState.value = LoadMoreState(true, null)
            //noinspection ConstantConditions
            nextPageLiveData?.observeForever(this)
        }


        override fun onChanged(result: Resource<Boolean>?) {
            if (result == null)
                reset()
            else{
                when (result.status) {
                    Status.SUCCESS -> {
                        hasMore = true == result.data
                        unregister()
                        loadMoreState.setValue(LoadMoreState(false, null))
                    }
                    Status.ERROR -> {
                        hasMore = true
                        unregister()
                        loadMoreState.setValue(LoadMoreState(false,
                                result.message))
                    }
                    else ->{
                        println("failed to find a status in next page handler")
                    }
                }
            }
        }

        fun unregister(){
            if (nextPageLiveData != null){
                nextPageLiveData!!.removeObserver(this)
                nextPageLiveData = null
                if (hasMore){
                    query = ""
                }
            }
        }

        fun reset(){
            unregister()
            hasMore = true
            loadMoreState.value = LoadMoreState(false, null)
        }

    }
}