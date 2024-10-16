package com.maliks.applocker.xtreme.ui.vault.vaultlist

import com.maliks.applocker.xtreme.data.database.vault.VaultMediaEntity

data class VaultListItemViewState(val vaultMediaEntity: VaultMediaEntity) {

    fun getDecryptedCachePath() = vaultMediaEntity.decryptedPreviewCachePath
}