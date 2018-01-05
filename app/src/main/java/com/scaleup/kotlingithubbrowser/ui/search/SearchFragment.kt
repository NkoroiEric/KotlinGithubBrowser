package com.scaleup.kotlingithubbrowser.ui.search

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.binding.FragmentDataBindingComponent
import com.scaleup.kotlingithubbrowser.databinding.SearchFragmentBinding
import com.scaleup.kotlingithubbrowser.di.Injectable
import com.scaleup.kotlingithubbrowser.ui.common.NavigationController
import com.scaleup.kotlingithubbrowser.ui.common.RepoClickCallback
import com.scaleup.kotlingithubbrowser.ui.common.RepoListAdapter
import com.scaleup.kotlingithubbrowser.ui.common.RetryCallback
import com.scaleup.kotlingithubbrowser.util.AutoClearedValue
import javax.inject.Inject

class SearchFragment : Fragment() , Injectable{

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var navigationController : NavigationController

    private val searchViewModel :SearchViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
    }

    private val dataBindingComponent = FragmentDataBindingComponent(this)
    private lateinit var binding : AutoClearedValue<SearchFragmentBinding>
    private lateinit var adapter : AutoClearedValue<RepoListAdapter>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val dataBinding = DataBindingUtil.inflate<SearchFragmentBinding>(inflater, R.layout.search_fragment, container, false)
        binding = AutoClearedValue(this, dataBinding)
        return dataBinding?.root
    }

    override  fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        val repoListAdapter = RepoListAdapter(dataBindingComponent, true, RepoClickCallback{
            repo -> navigationController.navigateToRepo(repo.owner.login, repo.name)
        })
        binding.get()?.repoList?.adapter = repoListAdapter
        adapter = AutoClearedValue(this, repoListAdapter)
        initSearchInputListener()
        binding.get()?.callback = object : RetryCallback {
            override fun retry() {
                searchViewModel.refresh()
            }
        }
    }

    private fun initSearchInputListener() {
        binding.get()?.input?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                doSearch(v)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.get()?.input?.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)){
                doSearch(v)
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }


    private fun initRecyclerView() {
        println("init recycler view")
        binding.get()!!.repoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                println("init recycler view onscrolled")
                val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager
                        .findLastVisibleItemPosition()
                if (lastPosition == adapter.get()!!.itemCount - 1) {
                    searchViewModel.loadNextPage()
                }
            }
        })
        searchViewModel.results.observe(this, Observer{ result ->
            println("init recycler view results")
            binding.get()!!.searchResource = result
            binding.get()!!.resultCount = if (result?.data == null)
                0
            else
                result.data!!.size
            result?.data?.forEach {
                println(it.id)
            }
            adapter.get()!!.replace(result?.data)
            binding.get()!!.executePendingBindings()
        })

        searchViewModel.loadMoreStatus().observe(this, Observer{ loadingMore ->
            println("init recycler view loadmore")
            if (loadingMore == null) {
                println("init recycler view loadmore false")
                binding.get()!!.loadingMore = false
            } else {
                println("init recycler view loadmore true")
                binding.get()!!.loadingMore = loadingMore.isRunning
                val error = loadingMore.errorMessageIfNotHandled
                if (error != null) {
                    Snackbar.make(binding.get()!!.loadMoreBar, error.toString(), Snackbar.LENGTH_LONG).show()
                }
            }
            binding.get()!!.executePendingBindings()
        })
    }

    private fun doSearch(v: View) {
        val query = binding.get()!!.input.text.toString()
        // Dismiss keyboard
        dismissKeyboard(v.windowToken)
        binding.get()!!.query = query
        searchViewModel.setQuery(query)
    }


    private fun dismissKeyboard(windowToken: IBinder) {
        val activity = activity
        if (activity != null) {
            val imm = activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

}
