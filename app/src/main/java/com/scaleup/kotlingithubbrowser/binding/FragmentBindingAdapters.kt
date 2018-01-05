package com.scaleup.kotlingithubbrowser.binding


import android.databinding.BindingAdapter
import android.support.v4.app.Fragment
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.scaleup.kotlingithubbrowser.R
import javax.inject.Inject


/**
 * Binding adapters that work with a fragment instance.
 */
class FragmentBindingAdapters @Inject
    constructor(internal val fragment: Fragment) {
    @BindingAdapter("imageUrl")
    fun bindImage(imageView: ImageView, url: String?) {
        if (url == null){
            Glide.with(fragment).load(R.drawable.ic_avatar).into(imageView)
        }else {
            Glide.with(fragment).load(url).into(imageView)
        }
    }
}
