package com.maliks.applocker.xtreme.di.module

import com.iammert.hdwallpapers.di.scope.FragmentScope
import com.maliks.applocker.xtreme.data.database.callblocker.addtoblacklist.AddToBlackListDialog
import com.maliks.applocker.xtreme.ui.browser.bookmarks.BookmarksDialog
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.delete.BlackListItemDeleteDialog
import com.maliks.applocker.xtreme.ui.permissiondialog.UsageAccessPermissionDialog
import com.maliks.applocker.xtreme.ui.policydialog.PrivacyPolicyDialog
import com.maliks.applocker.xtreme.ui.rateus.RateUsDialog
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.AddToVaultDialog
import com.maliks.applocker.xtreme.ui.vault.removingvaultdialog.RemoveFromVaultDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DialogFragmentBuilderModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun rateUsDialogFragment(): RateUsDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun permissionDialogFragment(): UsageAccessPermissionDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun privacyPolicyDialogFragment(): PrivacyPolicyDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bookmarksDialogFragment(): BookmarksDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun addToVaultDialog(): AddToVaultDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun addToBlackListDialog(): AddToBlackListDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun deleteBlackListItemDialog(): BlackListItemDeleteDialog

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun removeFromVaultDialog(): RemoveFromVaultDialog
}