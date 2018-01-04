package com.scaleup.kotlingithubbrowser.binding

import android.databinding.DataBindingComponent
import android.support.v4.app.Fragment


class FragmentDataBindingComponent(fragment : Fragment) : DataBindingComponent {
    override fun getBindingAdapters(): BindingAdapters {
        return BindingAdapters
    }

    private val adapter = FragmentBindingAdapters(fragment)

    override fun getFragmentBindingAdapters(): FragmentBindingAdapters = adapter
}