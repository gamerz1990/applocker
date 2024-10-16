package com.maliks.applocker.xtreme.data

import android.graphics.drawable.Drawable
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppEntity

data class AppData(val appName: String, val packageName: String, val appIconDrawable: Drawable) {

    fun parsePackageName() : String{
        if(packageName.indexOf("/") > -1) {
            return packageName.substring(0, packageName.indexOf("/"))
        }
        return packageName;
    }

    fun toEntity(): LockedAppEntity {
        return LockedAppEntity(packageName = packageName)
    }
}