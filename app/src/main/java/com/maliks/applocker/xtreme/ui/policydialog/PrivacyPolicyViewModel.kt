package com.maliks.applocker.xtreme.ui.policydialog

import com.maliks.applocker.xtreme.data.AppLockerPreferences
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import javax.inject.Inject

class PrivacyPolicyViewModel @Inject constructor(val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {

    fun acceptPrivacyPolicy() {
        appLockerPreferences.acceptPrivacyPolicy()
    }

}