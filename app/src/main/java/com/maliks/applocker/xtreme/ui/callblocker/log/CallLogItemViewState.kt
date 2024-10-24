package com.maliks.applocker.xtreme.ui.callblocker.log

import android.content.Context
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.callblocker.calllog.CallLogItemEntity
import com.maliks.applocker.xtreme.util.extensions.getCurrentLocale
import org.ocpsoft.prettytime.PrettyTime

data class CallLogItemViewState(val callLogItemEntity: CallLogItemEntity) {

    fun getUserFirstLetter(context: Context): String {
        val unknownNumber = context.getString(R.string.title_name_unknown_number)
        return if (callLogItemEntity.userName.isNullOrEmpty()) {
            unknownNumber.substring(0, 1).toUpperCase()
        } else {
            callLogItemEntity.userName.substring(0, 1).toUpperCase()
        }
    }

    fun getUserName(context: Context): String {
        val unknownNumber = context.getString(R.string.title_name_unknown_number)
        val userName = if (callLogItemEntity.userName.isNullOrEmpty()) {
            unknownNumber
        } else {
            callLogItemEntity.userName
        }

        return userName
    }

    fun getPhoneNumber(): String {
        return callLogItemEntity.phoneNumber
    }

    fun getPrettyTimeText(context: Context): String {
        val p = PrettyTime(context.getCurrentLocale())
        return p.format(callLogItemEntity.logDate)
    }
}