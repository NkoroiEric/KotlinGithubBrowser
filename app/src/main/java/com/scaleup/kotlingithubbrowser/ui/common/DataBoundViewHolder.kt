package com.scaleup.kotlingithubbrowser.ui.common

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

class DataBoundViewHolder<out T : ViewDataBinding>(binding: T) : RecyclerView.ViewHolder(binding.root){
    val binding : T = binding
}