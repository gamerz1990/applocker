package com.maliks.applocker.xtreme.util.binding

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.maliks.applocker.xtreme.R
import com.squareup.picasso.Picasso
import java.io.File

@BindingAdapter("srcCompat")
fun setVectorDrawable(imageView: AppCompatImageView, drawable: Drawable?) {
    drawable?.let { imageView.setImageDrawable(drawable) }
}

@BindingAdapter("url")
fun loadUrl(imageView: AppCompatImageView, url: String) {
    if (url.isNotEmpty()) {
        Picasso.Builder(imageView.context).build()
            .load(url)
            .placeholder(R.drawable.placeholder)
            .into(imageView)
    }
}

@BindingAdapter(value = ["file", "imageSize"], requireAll = false)
fun loadFile(imageView: AppCompatImageView, url: String, imageSize: ImageSize?) {
    if (url.isNotEmpty()) {
        val picassoRequestCreator = Picasso.Builder(imageView.context).build()
            .load(File(url))
            .placeholder(R.drawable.placeholder)

        if (imageSize != null && imageSize != ImageSize.ORIGINAL) {
            picassoRequestCreator
                .resize(0, imageSize.size.toInt())
        }

        picassoRequestCreator.into(imageView)
    }
}

@BindingAdapter(value = ["imageUri", "imageSize"], requireAll = false)
fun loadUri(imageView: AppCompatImageView, uri: Uri?, imageSize: ImageSize?) {
    if (uri != null) {
        val picassoRequestCreator = Picasso.Builder(imageView.context).build()
            .load(uri)
            .placeholder(R.drawable.placeholder)

        if (imageSize != null && imageSize != ImageSize.ORIGINAL) {
            picassoRequestCreator
                .resize(0, imageSize.size.toInt())
        }

        picassoRequestCreator.into(imageView)
    } else {
        // Clear the image if URI is null
        imageView.setImageDrawable(null)
    }
}
