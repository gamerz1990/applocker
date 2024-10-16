package com.maliks.applocker.xtreme.data.database.lockedapps

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_app")
data class LockedAppEntity(@PrimaryKey @ColumnInfo(name = "packageName") val packageName: String) {

    fun parsePackageName() : String{
        if(packageName.indexOf("/") > -1) {
            return packageName.substring(0, packageName.indexOf("/"))
        } else {
            return packageName;
        }
    }
}