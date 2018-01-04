package com.scaleup.kotlingithubbrowser.ui.repo

import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.databinding.ContributorItemBinding
import com.scaleup.kotlingithubbrowser.ui.common.DataBoundListAdapter
import com.scaleup.kotlingithubbrowser.util.Objects
import com.scaleup.kotlingithubbrowser.vo.Contributor

class ContributorAdapter(dataBindingComponent: DataBindingComponent, callback : ContributorClickCallback?) : DataBoundListAdapter<Contributor, ContributorItemBinding>() {

    private val component = dataBindingComponent
    private val callback = callback

    override fun createBinding(parent: ViewGroup): ContributorItemBinding {
        val binding = DataBindingUtil.inflate<ContributorItemBinding>(LayoutInflater.from(parent.context), R.layout.contributor_item, parent, false, component)
        binding!!.root.setOnClickListener {
            val contributor  = binding.contributor
            if (contributor != null && callback != null){
                callback.onClick(contributor)
            }
        }
        return binding
    }


    override fun bind(binding: ContributorItemBinding, item: Contributor) {
        binding.contributor = item
    }

    override fun areItemsTheSame(oldItem: Contributor, newItem: Contributor): Boolean {
        return Objects.equals(oldItem.login, newItem.login)
    }

    override fun areContentsTheSame(oldItem: Contributor, newItem: Contributor): Boolean {
        return Objects.equals(oldItem.avatarUrl, newItem.avatarUrl) &&
                oldItem.contributions == newItem.contributions
    }

    interface ContributorClickCallback {
        fun onClick(contributor: Contributor)
    }

}