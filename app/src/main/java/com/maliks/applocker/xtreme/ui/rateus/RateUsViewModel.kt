package com.maliks.applocker.xtreme.ui.rateus

import com.maliks.applocker.xtreme.repository.UserPreferencesRepository
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import javax.inject.Inject

class RateUsViewModel @Inject constructor(val userPreferencesRepository: UserPreferencesRepository) :
    RxAwareViewModel() {

    fun setUserRateUs() = userPreferencesRepository.setUserRateUs()
}