package com.maliks.applocker.xtreme.util.encryptor

import android.content.Context
import android.net.Uri
import com.facebook.android.crypto.keychain.AndroidConceal
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.CryptoConfig
import com.facebook.crypto.Entity
import com.maliks.applocker.xtreme.util.helper.file.FileManager
import com.maliks.applocker.xtreme.util.helper.file.FileOperationRequest
import io.reactivex.Observable
import java.io.*
import javax.inject.Inject

class FileEncryptor @Inject constructor(
    context: Context,
    private val fileManager: FileManager
) {

    private val appContext: Context = context.applicationContext

    private val keyChain = SharedPrefsBackedKeyChain(appContext, CryptoConfig.KEY_256)

    private val crypto = AndroidConceal.get().createDefaultCrypto(keyChain)

    fun encrypt(
        inputStream: InputStream,
        encryptFileOperationRequest: EncryptFileOperationRequest
    ): Observable<CryptoProcess> {
        return Observable.create { emitter ->
            emitter.onNext(CryptoProcess.processing(0))

            val outputFile = FileOperationRequest(
                fileName = encryptFileOperationRequest.fileName,
                directoryType = encryptFileOperationRequest.directoryType,
                fileExtension = encryptFileOperationRequest.fileExtension
            ).run { fileManager.createFile(this, FileManager.SubFolder.VAULT) }

            val fileOutputStream = BufferedOutputStream(FileOutputStream(outputFile))
            val cryptoOutputStream =
                crypto.getCipherOutputStream(fileOutputStream, Entity.create("entity_id"))

            val buffer = ByteArray(1024)
            var read = inputStream.read(buffer)
            var totalRead = 0L

            while (read != -1) {
                cryptoOutputStream.write(buffer, 0, read)
                totalRead += read
                val percent = calculateProgress(totalRead, inputStream.available().toLong())
                emitter.onNext(CryptoProcess.processing(percent))
                read = inputStream.read(buffer)
            }

            cryptoOutputStream.close()
            inputStream.close()

            emitter.onNext(CryptoProcess.complete(outputFile))
            emitter.onComplete()
        }
    }

    fun decrypt(
        encryptedFile: File,
        decryptFileOperationRequest: FileOperationRequest
    ): Observable<CryptoProcess> {
        return Observable.create { emitter ->
            emitter.onNext(CryptoProcess.processing(0))

            val outputFile = FileOperationRequest(
                fileName = decryptFileOperationRequest.fileName,
                directoryType = decryptFileOperationRequest.directoryType,
                fileExtension = decryptFileOperationRequest.fileExtension
            ).run { fileManager.createFile(this, FileManager.SubFolder.VAULT) }

            val fileInputStream = FileInputStream(encryptedFile)
            val fileCryptoInputStream =
                crypto.getCipherInputStream(fileInputStream, Entity.create("entity_id"))

            val fileOutputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var read = fileCryptoInputStream.read(buffer)
            var totalRead = 0L

            while (read != -1) {
                fileOutputStream.write(buffer, 0, read)
                totalRead += read
                val percent = calculateProgress(totalRead, encryptedFile.length())
                emitter.onNext(CryptoProcess.processing(percent))
                read = fileCryptoInputStream.read(buffer)
            }

            fileCryptoInputStream.close()
            fileInputStream.close()
            fileOutputStream.close()

            emitter.onNext(CryptoProcess.complete(outputFile))
            emitter.onComplete()
        }
    }

    private fun calculateProgress(totalRead: Long, totalSize: Long): Int {
        return if (totalSize > 0) {
            ((totalRead * 100) / totalSize).toInt()
        } else {
            0
        }
    }
}
