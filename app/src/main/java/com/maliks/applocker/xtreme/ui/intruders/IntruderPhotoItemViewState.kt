package com.maliks.applocker.xtreme.ui.intruders

import android.net.Uri
import com.maliks.applocker.xtreme.util.binding.ImageSize

data class IntruderPhotoItemViewState(private val photoUri: Uri) {

    // Return the Uri itself for use in image loading
    fun getPhotoUri(): Uri = photoUri

    // Return the Uri as a String if needed
    fun getUriString(): String = photoUri.toString()

    // Get the image size for UI purposes
    fun getImageSize(): ImageSize = ImageSize.MEDIUM
}
