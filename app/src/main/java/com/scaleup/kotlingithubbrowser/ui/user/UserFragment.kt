package com.scaleup.kotlingithubbrowser.ui.user

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.binding.FragmentDataBindingComponent
import com.scaleup.kotlingithubbrowser.databinding.UserFragmentBinding
import com.scaleup.kotlingithubbrowser.di.Injectable
import com.scaleup.kotlingithubbrowser.ui.common.NavigationController
import com.scaleup.kotlingithubbrowser.ui.common.RepoClickCallback
import com.scaleup.kotlingithubbrowser.ui.common.RepoListAdapter
import com.scaleup.kotlingithubbrowser.ui.common.RetryCallback
import com.scaleup.kotlingithubbrowser.util.AutoClearedValue
import javax.inject.Inject

const val  LOGIN_KEY = "login";
class UserFragment : Fragment() , Injectable {

    @Inject
    lateinit var  viewModelFactory : ViewModelProvider.Factory
    @Inject
    lateinit var navigationController: NavigationController

    private val dataBindingComponent = FragmentDataBindingComponent(this);
    private val userViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
    }
    @VisibleForTesting
    private lateinit var binding: AutoClearedValue<UserFragmentBinding>
    private lateinit var adapter:AutoClearedValue<RepoListAdapter>


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val  dataBinding = DataBindingUtil.inflate<UserFragmentBinding>(inflater!!, R.layout.user_fragment,
                container, false, dataBindingComponent);
        dataBinding!!.retryCallback = object : RetryCallback {
            override fun retry(){
                userViewModel.retry()
            }
        }
        binding = AutoClearedValue(this, dataBinding);
        return dataBinding.getRoot();
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState);
        userViewModel.login.value =  arguments.getString(LOGIN_KEY)
        userViewModel.getUser().observe(this, Observer {  userResource ->
            binding.get()!!.user = userResource?.data
            binding.get()!!.setUserResource(userResource);
            // this is only necessary because espresso cannot read data binding callbacks.
            binding.get()!!.executePendingBindings();
        });
        val rvAdapter = RepoListAdapter(dataBindingComponent, false,
                RepoClickCallback {  repo -> navigationController.navigateToRepo(repo.owner.login, repo.name)})
        binding.get()!!.repoList.setAdapter(rvAdapter);
        this.adapter = AutoClearedValue(this, rvAdapter);
        initRepoList();
    }

    private fun initRepoList() {
        userViewModel.getRepositories().observe(this, Observer { repos ->
                // no null checks for adapter.get() since LiveData guarantees that we'll not receive
                // the event if fragment is now show.
                if (repos == null) {
                    adapter.get()!!.replace(null);
                } else {
                    adapter.get()!!.replace(repos.data);
                }
            });
    }
}
