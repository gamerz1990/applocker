package com.maliks.applocker.xtreme.ui.vault.removingvaultdialog

import android.app.Application
import android.os.Handler
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaEntity
import com.maliks.applocker.xtreme.repository.VaultRepository
import com.maliks.applocker.xtreme.ui.RxAwareAndroidViewModel
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.AddToVaultViewState
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.ProcessState
import com.maliks.applocker.xtreme.util.encryptor.CryptoProcess
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import com.maliks.applocker.xtreme.util.helper.file.MediaScannerConnector
import com.maliks.applocker.xtreme.util.helper.progress.FakeProgress
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RemoveFromVaultViewModel @Inject constructor(
    private val app: Application,
    private val vaultRepository: VaultRepository
) : RxAwareAndroidViewModel(app) {

    private val removeFromVaultProcessLiveData = MutableLiveData<CryptoProcess>()

    private val fakeProgress = FakeProgress()

    private val removeFromVaultViewStateLiveData =
        MutableLiveData<RemoveFromVaultViewState>()
            .apply {
                value =
                    RemoveFromVaultViewState(progress = 0, processState = ProcessState.PROCESSING)
            }


    init {
        with(fakeProgress) {
            setOnProgressListener {
                removeFromVaultViewStateLiveData.value =
                    RemoveFromVaultViewState(it, ProcessState.PROCESSING)
            }

            setOnCompletedListener {
                removeFromVaultViewStateLiveData.value =
                    RemoveFromVaultViewState(100, ProcessState.COMPLETE)
            }
        }
    }

    fun removeMediaFromVault(vaultMediaEntity: VaultMediaEntity) {
        val decryptionObservable = vaultRepository
            .removeMediaFromVault(vaultMediaEntity)
            .doOnComplete { refreshFileSystem(vaultMediaEntity.originalUri) }

        disposables += decryptionObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { fakeProgress.start() }
            .subscribe(
                {
                    when (it) {
                        is CryptoProcess.Complete -> fakeProgress.complete()
                        is CryptoProcess.Processing -> TODO()
                    }
                },
                { FirebaseCrashlytics.getInstance().recordException(it) })
    }

    fun getRemoveFromVaultViewStateLiveData() = removeFromVaultViewStateLiveData

    private fun refreshFileSystem(originalPath: String) {
        MediaScannerConnector.scanByFilePath(app, originalPath)
    }
}