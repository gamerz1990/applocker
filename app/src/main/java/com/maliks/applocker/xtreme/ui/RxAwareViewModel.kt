package com.maliks.applocker.xtreme.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class RxAwareViewModel : ViewModel() {

    var disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}