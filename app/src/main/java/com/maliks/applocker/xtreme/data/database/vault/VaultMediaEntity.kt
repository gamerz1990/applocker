package com.maliks.applocker.xtreme.data.database.vault

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "vault_media")
data class VaultMediaEntity(
    @PrimaryKey @ColumnInfo(name = "original_uri") val originalUri: String,
    @ColumnInfo(name = "original_file_name") val originalFileName: String = "",
    @ColumnInfo(name = "encrypted_path") val encryptedPath: String = "",
    @ColumnInfo(name = "encrypted_preview_path") val encryptedPreviewPath: String = "",
    @ColumnInfo(name = "media_type") val mediaType: String = ""
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    var decryptedPreviewCachePath: String = ""

    /**
     * Extracts the encrypted preview file name from the encrypted preview path.
     */
    fun getEncryptedPreviewFileName(): String {
        val slashIndex = encryptedPreviewPath.lastIndexOf('/')
        return if (slashIndex != -1) {
            encryptedPreviewPath.substring(slashIndex + 1)
        } else {
            encryptedPreviewPath
        }
    }
}
