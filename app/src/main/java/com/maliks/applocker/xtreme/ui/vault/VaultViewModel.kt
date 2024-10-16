package com.maliks.applocker.xtreme.ui.vault

import com.maliks.applocker.xtreme.repository.UserPreferencesRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import javax.inject.Inject

class VaultViewModel @Inject constructor(val userPreferencesRepository: UserPreferencesRepository) : RxAwareViewModel() {

    fun shouldShowRateUs(): Boolean {
        return userPreferencesRepository.isUserRateUs().not()
    }

    fun setRateUsAsked() {
        userPreferencesRepository.setRateUsAsked()
    }
}