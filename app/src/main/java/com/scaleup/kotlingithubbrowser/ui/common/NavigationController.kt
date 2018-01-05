package com.scaleup.kotlingithubbrowser.ui.common

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.scaleup.kotlingithubbrowser.MainActivity
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.ui.repo.REPO_NAME_KEY
import com.scaleup.kotlingithubbrowser.ui.repo.REPO_OWNER_KEY
import com.scaleup.kotlingithubbrowser.ui.repo.RepoFragment
import com.scaleup.kotlingithubbrowser.ui.search.SearchFragment
import com.scaleup.kotlingithubbrowser.ui.user.LOGIN_KEY
import com.scaleup.kotlingithubbrowser.ui.user.UserFragment
import kotlinx.android.synthetic.main.main_activity.view.*
import javax.inject.Inject

/**
 * A utility class that handles navigation in {@link MainActivity}.
 */
class NavigationController @Inject constructor(mainActivity: MainActivity) {
    private val containerId = R.id.container
    private val fragmentManager : FragmentManager = mainActivity.supportFragmentManager

    fun navigateToSearch() {
        val search = SearchFragment()
        fragmentManager.replace(containerId, search)
    }

    fun navigateToUser(login :String){
        val user = UserFragment()
        val tag = "user" + "/" + login
        val bundle = Bundle()
        bundle.putString(LOGIN_KEY, login)
        user.arguments = bundle
        fragmentManager.replace(containerId, user, tag)
    }

    fun navigateToRepo(owner: String, name : String){
        val repo = RepoFragment()
        val tag = "repo/$owner/$name"
        val bundle = Bundle()
        bundle.putString(REPO_OWNER_KEY, owner)
        bundle.putString(REPO_NAME_KEY, name)
        repo.arguments = bundle
        fragmentManager.replace(containerId, repo, tag)
    }

    private fun FragmentManager.replace(id : Int, fragment: Fragment){
        beginTransaction().replace(id, fragment).commitAllowingStateLoss()
    }

    inline fun <reified T>FragmentManager.replace(id : Int, fragment: T, tag : String){
        beginTransaction()
                .replace(id, fragment as Fragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }
}