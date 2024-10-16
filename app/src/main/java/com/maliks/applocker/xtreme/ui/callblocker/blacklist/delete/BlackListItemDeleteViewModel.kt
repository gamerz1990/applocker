package com.maliks.applocker.xtreme.ui.callblocker.blacklist.delete

import com.maliks.applocker.xtreme.repository.CallBlockerRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BlackListItemDeleteViewModel @Inject constructor(val callBlockerRepository: CallBlockerRepository) :
    RxAwareViewModel() {

    fun deleteFromBlackList(blackListItemId: Int) {
        disposables += callBlockerRepository.deleteBlackListItem(blackListItemId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

}