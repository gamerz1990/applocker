package com.maliks.applocker.xtreme.data.database.callblocker.addtoblacklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.database.callblocker.blacklist.BlackListItemEntity
import com.maliks.applocker.xtreme.repository.CallBlockerRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddToBlackListViewModel @Inject constructor(val callBlockerRepository: CallBlockerRepository) :
    RxAwareViewModel() {

    private val addToBlacklistViewStateLiveData = MutableLiveData<AddToBlacklistViewState>()

    fun blockNumber(name: String, phoneNumber: String) {
        val userName = if (name.isNullOrEmpty()) DEFAULT_NAME else name
        val blackListItemEntity = BlackListItemEntity(userName = userName, phoneNumber = phoneNumber)
        disposables += callBlockerRepository.addToBlackList(blackListItemEntity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { addToBlacklistViewStateLiveData.value = AddToBlacklistViewState(BlockState.BLOCKED) },
                { t -> addToBlacklistViewStateLiveData.value = AddToBlacklistViewState(BlockState.ERROR) })
    }

    fun getViewStateLiveData(): LiveData<AddToBlacklistViewState> = addToBlacklistViewStateLiveData

    companion object {

        private const val DEFAULT_NAME = ""
    }
}