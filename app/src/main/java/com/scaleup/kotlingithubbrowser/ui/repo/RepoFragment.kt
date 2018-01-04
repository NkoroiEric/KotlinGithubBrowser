package com.scaleup.kotlingithubbrowser.ui.repo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.binding.FragmentDataBindingComponent
import com.scaleup.kotlingithubbrowser.databinding.ContributorItemBinding
import com.scaleup.kotlingithubbrowser.databinding.RepoFragmentBinding
import com.scaleup.kotlingithubbrowser.di.Injectable
import com.scaleup.kotlingithubbrowser.ui.common.NavigationController
import com.scaleup.kotlingithubbrowser.ui.common.RetryCallback
import com.scaleup.kotlingithubbrowser.ui.repo.ContributorAdapter.*
import com.scaleup.kotlingithubbrowser.util.AutoClearedValue
import com.scaleup.kotlingithubbrowser.vo.Contributor
import javax.inject.Inject

class RepoFragment : Fragment() , Injectable{

    @Inject lateinit var viewModelFactory : ViewModelProvider.Factory
    //lazily init viewmodel and no need for thread safety since it
    //always called from main thread
    private val repoViewModel  by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, viewModelFactory).get(RepoViewModel::class.java)
    }

    private lateinit var binding : AutoClearedValue<RepoFragmentBinding>
    private lateinit var adapter : AutoClearedValue<ContributorAdapter>
    @Inject lateinit var navigationController : NavigationController

    val dataBindingComponent = FragmentDataBindingComponent(this)

    private val REPO_OWNER_KEY = "repo_owner"
    private val REPO_NAME_KEY = "repo_name"

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            if(it.keySet().containsAll(listOf(REPO_NAME_KEY, REPO_OWNER_KEY))){
                repoViewModel.setId(it.getString(REPO_OWNER_KEY), it.getString(REPO_NAME_KEY))
            }else {
                repoViewModel.setId(null, null)
            }
        }

        val repo = repoViewModel.repo
        repo.observe(this, Observer { r ->
            binding.get()!!.repo = r!!.data
            binding.get()!!.repoResource = r
            binding.get()!!.executePendingBindings()
        })

        val adapter = ContributorAdapter(dataBindingComponent,object : ContributorClickCallback{
            override fun onClick(contributor: Contributor) {
                navigationController.navigateToUser(contributor.login)
            }
        })
        this.adapter = AutoClearedValue(this, adapter)
        binding.get()?.contributorList?.adapter = adapter
        initContributorList(repoViewModel)
    }

    fun initContributorList(repoViewModel: RepoViewModel){
        repoViewModel.contributors.observe(this, Observer{listResource ->
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null){
                adapter.get()?.replace(listResource.data)
            }else {
                //constant condition
                adapter.get()?.replace(emptyList())
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val dataBinding:RepoFragmentBinding? = DataBindingUtil.inflate<RepoFragmentBinding>(inflater, R.layout.repo_fragment, container, false)
        dataBinding?.let {
            it.retryCallback = object : RetryCallback{
                override fun retry() {
                    repoViewModel.retry()
                }
            }
            binding = AutoClearedValue(this, dataBinding)
        }
        return dataBinding?.root
    }
}
