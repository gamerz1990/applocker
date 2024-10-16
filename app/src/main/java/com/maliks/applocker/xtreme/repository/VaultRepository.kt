package com.maliks.applocker.xtreme.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaDao
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaEntity
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType
import com.maliks.applocker.xtreme.util.encryptor.CryptoProcess
import com.maliks.applocker.xtreme.util.encryptor.EncryptFileOperationRequest
import com.maliks.applocker.xtreme.util.encryptor.FileEncryptor
import com.maliks.applocker.xtreme.util.extensions.doOnBackground
import com.maliks.applocker.xtreme.util.helper.file.FileManager
import com.maliks.applocker.xtreme.util.helper.file.FileOperationRequest
import com.maliks.applocker.xtreme.util.helper.file.DirectoryType
import com.maliks.applocker.xtreme.util.helper.file.FileExtension
import com.maliks.applocker.xtreme.util.helper.file.PreviewCreator
import io.reactivex.Observable
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val context: Context,
    private val vaultMediaDao: VaultMediaDao,
    private val fileEncryptor: FileEncryptor,
    private val fileManager: FileManager,
    private val previewCreator: PreviewCreator
) {

    fun getVaultImages(): Flowable<List<VaultMediaEntity>> {
        return vaultMediaDao.getVaultImages()
            .flatMap { list ->
                Flowable.fromIterable(list)
                    .flatMap { vaultMediaEntity -> Flowable.just(vaultMediaEntity) }
                    .flatMap { getPreviewFile(it) }
                    .toList()
                    .toFlowable()
            }
    }

    fun getVaultVideos(): Flowable<List<VaultMediaEntity>> {
        return vaultMediaDao.getVaultVideos()
            .flatMap { list ->
                Flowable.fromIterable(list)
                    .flatMap { vaultMediaEntity -> Flowable.just(vaultMediaEntity) }
                    .flatMap { getPreviewFile(it) }
                    .toList()
                    .toFlowable()
            }
    }

    fun addMediaToVault(
        uri: Uri,
        mediaType: VaultMediaType
    ): Observable<CryptoProcess> {

        return Observable.create { emitter ->

            try {
                // Open an InputStream from the URI
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Unable to open input stream from URI")

                // Get the original file name from the URI
                val originalFileName = getFileNameFromUri(uri) ?: "unknown_file"

                // Create a unique encrypted file name
                val encryptedFileName = createFileName(mediaType)

                // Prepare the encryption request
                val encryptFileOperationRequest = EncryptFileOperationRequest(
                    fileName = encryptedFileName,
                    fileExtension = FileExtension.NONE,
                    directoryType = DirectoryType.EXTERNAL
                )

                // Encrypt the original file
                val originalEncryptionObservable = fileEncryptor.encrypt(
                    inputStream,
                    encryptFileOperationRequest,

                )

                // Generate and encrypt the preview file
                val previewEncryptionObservable = getPreviewEncryptionObservable(uri, encryptedFileName, mediaType)

                // Combine the encryption processes
                Observable.combineLatest(
                    originalEncryptionObservable,
                    previewEncryptionObservable,
                    BiFunction<CryptoProcess, CryptoProcess, CryptoProcess> { originalProcess, previewProcess ->

                        if (originalProcess is CryptoProcess.Complete && previewProcess is CryptoProcess.Complete) {
                            val vaultMediaEntity = VaultMediaEntity(
                                originalUri = uri.toString(),
                                originalFileName = originalFileName,
                                encryptedPath = originalProcess.file.absolutePath,
                                encryptedPreviewPath = previewProcess.file.absolutePath,
                                mediaType = mediaType.mediaType
                            )

                            // Insert into the database on a background thread
                            doOnBackground { vaultMediaDao.addToVault(vaultMediaEntity) }
                        }

                        val totalProgress = (getProgressPercentage(originalProcess) + getProgressPercentage(previewProcess)) / 2

                        if (originalProcess is CryptoProcess.Complete && previewProcess is CryptoProcess.Complete) {
                            originalProcess
                        } else {
                            CryptoProcess.processing(totalProgress)
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { process ->
                            emitter.onNext(process)
                            if (process is CryptoProcess.Complete) {
                                emitter.onComplete()
                            }
                        },
                        { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            emitter.onError(error)
                        }
                    )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                emitter.onError(e)
            }
        }
    }

    fun removeMediaFromVault(vaultMediaEntity: VaultMediaEntity): Observable<CryptoProcess> {
        return Observable.create { emitter ->
            try {
                val encryptedFile = File(vaultMediaEntity.encryptedPath)
                val decryptedFileName = vaultMediaEntity.originalFileName

                // Create a file in public storage to save the decrypted file
                val decryptedFileRequest = FileOperationRequest(
                    fileName = decryptedFileName,
                    directoryType = DirectoryType.EXTERNAL,
                    fileExtension = FileExtension.getFileExtensionFromName(decryptedFileName)
                )

                val decryptedFile = fileManager.createFile(decryptedFileRequest, FileManager.SubFolder.VAULT)
                    ?: throw Exception("Unable to create decrypted file")

                // Decrypt the file
                fileEncryptor.decrypt(
                    encryptedFile,
                    decryptedFileRequest
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { process ->
                            emitter.onNext(process)
                            if (process is CryptoProcess.Complete) {
                                // Remove the media entity from the database on a background thread
                                doOnBackground { vaultMediaDao.removeFromVault(vaultMediaEntity.originalUri) }
                                // Delete the encrypted files
                                encryptedFile.delete()
                                File(vaultMediaEntity.encryptedPreviewPath).delete()
                                emitter.onComplete()
                            }
                        },
                        { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            emitter.onError(error)
                        }
                    )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                emitter.onError(e)
            }
        }
    }

    private fun getPreviewEncryptionObservable(
        uri: Uri,
        encryptedFileName: String,
        mediaType: VaultMediaType
    ): Observable<CryptoProcess> {

        return Observable.create { emitter ->
            try {
                // Generate the preview file
                val previewFileName = createPreviewFileName(encryptedFileName)
                val previewCacheFileRequest = FileOperationRequest(
                    fileName = previewFileName,
                    fileExtension = FileExtension.JPEG,
                    directoryType = DirectoryType.EXTERNAL
                )

                val previewFile = when (mediaType) {
                    VaultMediaType.TYPE_IMAGE -> previewCreator.createPreviewImage(uri, previewCacheFileRequest)
                    VaultMediaType.TYPE_VIDEO -> previewCreator.createPreviewVideo(uri, previewCacheFileRequest)
                }

                // Encrypt the preview file
                val encryptPreviewRequest = EncryptFileOperationRequest(
                    fileName = previewFileName,
                    directoryType = DirectoryType.EXTERNAL,
                    fileExtension = FileExtension.NONE
                )

                fileEncryptor.encrypt(
                    previewFile.inputStream(),
                    encryptPreviewRequest
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { process ->
                            emitter.onNext(process)
                            if (process is CryptoProcess.Complete) {
                                emitter.onComplete()
                            }
                        },
                        { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            emitter.onError(error)
                        }
                    )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                emitter.onError(e)
            }
        }
    }

    private fun getPreviewFile(vaultMediaEntity: VaultMediaEntity): Flowable<VaultMediaEntity> {
        return Flowable.create({ emitter ->
            val previewCacheFileRequest = FileOperationRequest(
                fileName = vaultMediaEntity.getEncryptedPreviewFileName(),
                directoryType = DirectoryType.CACHE,
                fileExtension = FileExtension.JPEG
            )

            val previewCacheFile = fileManager.getFile(previewCacheFileRequest, FileManager.SubFolder.VAULT)

            if (previewCacheFile?.exists() == true) {
                vaultMediaEntity.decryptedPreviewCachePath = previewCacheFile.absolutePath
                emitter.onNext(vaultMediaEntity)
                emitter.onComplete()
            } else {
                createPreviewFile(vaultMediaEntity)
                    .subscribe(
                        { updatedEntity ->
                            emitter.onNext(updatedEntity)
                            emitter.onComplete()
                        },
                        { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            emitter.onError(error)
                        }
                    )
            }
        }, io.reactivex.BackpressureStrategy.BUFFER)
    }

    private fun createPreviewFile(vaultMediaEntity: VaultMediaEntity): Flowable<VaultMediaEntity> {
        return Flowable.create({ emitter ->
            try {
                val encryptedPreviewFile = File(vaultMediaEntity.encryptedPreviewPath)

                val decryptFileRequest = FileOperationRequest(
                    fileName = vaultMediaEntity.getEncryptedPreviewFileName(),
                    fileExtension = FileExtension.JPEG,
                    directoryType = DirectoryType.CACHE
                )

                fileEncryptor.decrypt(
                    encryptedPreviewFile,
                    decryptFileRequest
                )
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { process ->
                            if (process is CryptoProcess.Complete) {
                                vaultMediaEntity.decryptedPreviewCachePath = process.file.absolutePath
                                emitter.onNext(vaultMediaEntity)
                                emitter.onComplete()
                            }
                        },
                        { error ->
                            FirebaseCrashlytics.getInstance().recordException(error)
                            emitter.onError(error)
                        }
                    )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                emitter.onError(e)
            }
        }, io.reactivex.BackpressureStrategy.BUFFER)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }

    private fun createFileName(mediaType: VaultMediaType): String {
        return if (mediaType == VaultMediaType.TYPE_IMAGE) createEncryptedImageFileName() else createEncryptedVideoFileName()
    }

    private fun createEncryptedImageFileName(): String {
        return PREFIX_ENCRYPT_IMAGE + System.currentTimeMillis()
    }

    private fun createEncryptedVideoFileName(): String {
        return PREFIX_ENCRYPT_VIDEO + System.currentTimeMillis()
    }

    private fun createPreviewFileName(encryptedFileName: String): String {
        return "${PREFIX_PREVIEW}_$encryptedFileName"
    }

    private fun getProgressPercentage(process: CryptoProcess): Int {
        return when (process) {
            is CryptoProcess.Processing -> process.percentage
            is CryptoProcess.Complete -> 100
            else -> 0
        }
    }

    companion object {

        private const val PREFIX_ENCRYPT_IMAGE = "EIF"
        private const val PREFIX_ENCRYPT_VIDEO = "EVF"
        private const val PREFIX_PREVIEW = "PREVIEW"
    }
}
