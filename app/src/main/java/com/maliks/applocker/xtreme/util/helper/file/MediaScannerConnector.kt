package com.maliks.applocker.xtreme.util.helper.file

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore

class MediaScannerConnector(private val context: Context, private val filePath: String? = null, private val fileUri: Uri? = null) :
    MediaScannerConnection.MediaScannerConnectionClient {

    private val connection: MediaScannerConnection = MediaScannerConnection(context, this)
        .apply {
            connect()
        }

    override fun onMediaScannerConnected() {
        when {
            filePath != null -> connection.scanFile(filePath, null)
            fileUri != null -> {
                val filePathFromUri = getFilePathFromUri(context, fileUri) // Use context directly here
                filePathFromUri?.let {
                    connection.scanFile(it, null)
                } ?: run {
                    connection.disconnect()
                }
            }
        }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        connection.disconnect()
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    companion object {
        fun scanByFilePath(context: Context, filePath: String) = MediaScannerConnector(context, filePath)

        fun scanByUri(context: Context, fileUri: Uri) = MediaScannerConnector(context, fileUri = fileUri)
    }
}
