package com.maliks.applocker.xtreme.ui.vault.addingvaultdialog

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType
import com.maliks.applocker.xtreme.repository.VaultRepository
import com.maliks.applocker.xtreme.ui.RxAwareAndroidViewModel
import com.maliks.applocker.xtreme.util.encryptor.CryptoProcess
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import com.maliks.applocker.xtreme.util.helper.file.FileManager
import com.maliks.applocker.xtreme.util.helper.file.MediaScannerConnector
import com.maliks.applocker.xtreme.util.helper.progress.FakeProgress
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddToVaultViewModel @Inject constructor(
    private val app: Application,
    private val vaultRepository: VaultRepository,
    private val fileManager: FileManager
) : RxAwareAndroidViewModel(app) {

    private val fakeProgress = FakeProgress()

    private val addToVaultViewStateLiveData = MutableLiveData<AddToVaultViewState>()
        .apply {
            value = AddToVaultViewState(progress = 0, processState = ProcessState.PROCESSING)
        }

    init {
        with(fakeProgress) {
            setOnProgressListener {
                addToVaultViewStateLiveData.value = AddToVaultViewState(it, ProcessState.PROCESSING)
            }

            setOnCompletedListener {
                addToVaultViewStateLiveData.value = AddToVaultViewState(100, ProcessState.COMPLETE)
            }
        }
    }

    fun setSelectedUriList(selectedUriList: ArrayList<Uri>, mediaType: VaultMediaType) {

        val list = arrayListOf<Observable<CryptoProcess>>()

        for (uri in selectedUriList) {
            val encryptionObservable = vaultRepository
                .addMediaToVault(uri, mediaType)
                .doOnNext {
                    if (it is CryptoProcess.Complete) {
                        deleteAndRefreshFileSystem(uri)
                    }
                }

            list.add(encryptionObservable)
        }
        disposables += Observable.merge(list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { fakeProgress.start() }
            .doOnComplete { fakeProgress.complete() }
            .subscribe({}, { FirebaseCrashlytics.getInstance().recordException(it) })
    }

    fun getAddToVaultViewStateLiveData(): LiveData<AddToVaultViewState> =
        addToVaultViewStateLiveData

    private fun deleteAndRefreshFileSystem(uri: Uri) {
        disposables += fileManager.deleteFileFromUri(uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // Notify the media scanner about the deletion
                    MediaScannerConnector.scanByUri(context = app, uri)
                },
                { FirebaseCrashlytics.getInstance().recordException(it) })
    }
}
