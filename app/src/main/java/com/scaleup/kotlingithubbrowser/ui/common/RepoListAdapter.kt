package com.scaleup.kotlingithubbrowser.ui.common

import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.scaleup.kotlingithubbrowser.R
import com.scaleup.kotlingithubbrowser.databinding.RepoItemBinding
import com.scaleup.kotlingithubbrowser.util.Objects
import com.scaleup.kotlingithubbrowser.vo.Repo
import java.util.*

class RepoListAdapter(dataBindingComponent: DataBindingComponent, private val showFullName: Boolean, private val callback: RepoClickCallback?)
    : DataBoundListAdapter<Repo, RepoItemBinding>(){

    private val component = dataBindingComponent

    override fun createBinding(parent: ViewGroup): RepoItemBinding {
        val binding = DataBindingUtil.inflate<RepoItemBinding>(LayoutInflater.from(parent.context)
        , R.layout.repo_item, parent, false, component)
        binding?.showFullName = showFullName
        binding?.root?.setOnClickListener {
            val repo = binding.repo
            if (repo != null && callback != null){
                callback.onClick(repo)
            }
        }
        return binding!!
    }

    override fun bind(binding: RepoItemBinding, item: Repo) {
        println(item.toString())
        binding.repo = item
    }

    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return Objects.equals(oldItem.owner, newItem.owner) &&
                Objects.equals(oldItem.id, newItem.id)
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem == newItem
    }


}