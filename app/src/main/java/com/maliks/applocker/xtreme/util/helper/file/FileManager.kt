package com.maliks.applocker.xtreme.util.helper.file

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class FileManager @Inject constructor(private val context: Context) {

    enum class SubFolder(val subFolderPath: String) {
        VAULT("vault"),
        INTRUDERS("intruders")
    }

    fun createFile(fileOperationRequest: FileOperationRequest, subFolder: SubFolder): File {
        val folder = when (fileOperationRequest.directoryType) {
            DirectoryType.CACHE -> getCacheDir(subFolder)
            DirectoryType.EXTERNAL -> getAppSpecificExternalDir(subFolder)
         }

        if (!folder.exists()) {
            folder.mkdirs()
        }

        return File(
            folder,
            fileOperationRequest.fileName + fileOperationRequest.fileExtension.extension
        )
    }

    fun deleteFile(file: File): Completable {
        return Completable.create { emitter ->
            if (file.exists()) {
                if (file.delete()) {
                    emitter.onComplete()
                } else {
                    emitter.onError(Throwable("Failed to delete file"))
                }
            } else {
                emitter.onComplete()
            }
        }



    }

    fun deleteFileFromUri(uri: Uri): Completable {
        return Completable.create { emitter ->
            try {
                val rowsDeleted = context.contentResolver.delete(uri, null, null)
                if (rowsDeleted > 0) {
                    emitter.onComplete()
                } else {
                    emitter.onError(Throwable("Failed to delete file from Uri"))
                }
            } catch (e: Exception) {
                emitter.onError(Throwable("Exception while deleting file: ${e.message}"))
            }
        }
    }
    fun getFile(fileOperationRequest: FileOperationRequest, subFolder: SubFolder): File? {
        val folder = when (fileOperationRequest.directoryType) {
            DirectoryType.CACHE -> getCacheDir(subFolder)
            DirectoryType.EXTERNAL -> getAppSpecificExternalDir(subFolder)
        }

        val file = File(
            folder,
            fileOperationRequest.fileName + fileOperationRequest.fileExtension.extension
        )

        return if (file.exists()) file else null
    }

    fun getSubFiles(folder: File, extension: FileExtension): List<File> {
        if (!folder.exists() || folder.isFile) {
            return emptyList()
        }

        val files = folder.listFiles() ?: arrayOf()
        return files.filter { it.name.endsWith(extension.extension, ignoreCase = true) }
    }

    fun isFileExist(file: File): Boolean {
        return file.exists()
    }

    private fun getAppSpecificExternalDir(subFolder: SubFolder): File {
        val externalFilesDir = context.getExternalFilesDir(null)
            ?: context.filesDir  // Fallback to internal files directory if external is not available

        val folder = File(externalFilesDir, subFolder.subFolderPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    private fun getCacheDir(subFolder: SubFolder): File {
        val cacheDir = context.cacheDir
        val folder = File(cacheDir, subFolder.subFolderPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }
}
