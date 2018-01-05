package com.scaleup.kotlingithubbrowser.binding

import android.databinding.BindingAdapter
import android.view.View


object BindingAdapters {
    @BindingAdapter("visibleGone") @JvmStatic
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
}