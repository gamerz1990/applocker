package com.maliks.applocker.xtreme.ui.callblocker.blacklist

import android.content.Context
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.callblocker.blacklist.BlackListItemEntity

data class BlackListItemViewState(val blackListItemEntity: BlackListItemEntity) {

    fun getFirstLetter(context: Context): String {
        val unknownNumber = context.getString(R.string.title_name_unknown_number)
        return if (blackListItemEntity.userName.isNullOrEmpty()) {
            unknownNumber.substring(0, 1).toUpperCase()
        } else {
            blackListItemEntity.userName.substring(0, 1).toUpperCase()
        }
    }

    fun getName(context: Context): String {
        val unknownNumber = context.getString(R.string.title_name_unknown_number)
        return if (blackListItemEntity.userName.isNullOrEmpty()) {
            unknownNumber
        } else {
            blackListItemEntity.userName
        }
    }

    fun getNumber(): String {
        return blackListItemEntity.phoneNumber
    }
}