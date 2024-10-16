package com.maliks.applocker.xtreme.ui.vault.vaultlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaDao
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaEntity
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType.*
import com.maliks.applocker.xtreme.repository.VaultRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VaultListViewModel @Inject constructor(private val vaultMediaRepository: VaultRepository) : RxAwareViewModel() {

    private val vaultListViewStateLiveData = MutableLiveData<VaultListViewState>()

    private lateinit var vaultMediaType: VaultMediaType

    fun setVaultMediaType(vaultMediaType: VaultMediaType) {
        this.vaultMediaType = vaultMediaType
        vaultListViewStateLiveData.value = VaultListViewState.empty(vaultMediaType)

        val vaultMediaFlowable = when (vaultMediaType) {
            TYPE_IMAGE -> vaultMediaRepository.getVaultImages()
            TYPE_VIDEO -> vaultMediaRepository.getVaultVideos()
        }

        disposables += vaultMediaFlowable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                vaultListViewStateLiveData.value = VaultListViewState(
                    vaultMediaType = vaultMediaType,
                    vaultList = it
                )
            }
    }

    fun getVaultListViewStateLiveData(): LiveData<VaultListViewState> = vaultListViewStateLiveData


}