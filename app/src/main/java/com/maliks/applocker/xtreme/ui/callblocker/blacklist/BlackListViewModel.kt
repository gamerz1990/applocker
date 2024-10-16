package com.maliks.applocker.xtreme.ui.callblocker.blacklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.repository.CallBlockerRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BlackListViewModel @Inject constructor(val callBlockerRepository: CallBlockerRepository) : RxAwareViewModel() {

    private val blackListViewStateLiveData = MutableLiveData<BlackListViewState>()

    init {
        blackListViewStateLiveData.value = BlackListViewState.empty()

        disposables += callBlockerRepository.getBlackList()
            .map {
                val itemViewStateList = arrayListOf<BlackListItemViewState>()
                it.forEach { itemViewStateList.add(BlackListItemViewState(it)) }
                BlackListViewState(itemViewStateList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                blackListViewStateLiveData.value = it
            }
    }

    fun getViewStateLiveData(): LiveData<BlackListViewState> = blackListViewStateLiveData

}