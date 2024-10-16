package com.maliks.applocker.xtreme.util.helper.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PreviewCreator @Inject constructor(
    private val fileManager: FileManager,
    private val context: Context // Injecting context to work with Uri
) {

    fun createPreviewImage(originalMediaUri: Uri, fileOperationRequest: FileOperationRequest): File {
        val destinationFile = fileManager.createFile(fileOperationRequest, FileManager.SubFolder.VAULT)

        val fileOutputStream = FileOutputStream(destinationFile)

        val inputStream = context.contentResolver.openInputStream(originalMediaUri)
        val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
        val image = bitmap?.let { ThumbnailUtils.extractThumbnail(it, 250, 250, MediaStore.Images.Thumbnails.MINI_KIND) }

        if (image != null) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        } else {
            Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888).also {
                it.eraseColor(Color.GRAY)
                it.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            }
        }

        fileOutputStream.close()
        inputStream?.close()

        return destinationFile
    }

    fun createPreviewVideo(originalMediaUri: Uri, fileOperationRequest: FileOperationRequest): File {
        val destinationFile = fileManager.createFile(fileOperationRequest, FileManager.SubFolder.VAULT)

        val fileOutputStream = FileOutputStream(destinationFile)

        val videoPath = getVideoPathFromUri(originalMediaUri)
        val image = videoPath?.let { ThumbnailUtils.createVideoThumbnail(it, MediaStore.Images.Thumbnails.MINI_KIND) }

        if (image != null) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        } else {
            Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888).also {
                it.eraseColor(Color.GRAY)
                it.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            }
        }

        fileOutputStream.close()

        return destinationFile
    }

    private fun getVideoPathFromUri(uri: Uri): String? {
        // Retrieve the video path from the Uri
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()

        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val videoPath = columnIndex?.let { cursor.getString(it) }

        cursor?.close()

        return videoPath
    }
}
