package com.maliks.applocker.xtreme.di.module

import com.iammert.hdwallpapers.di.scope.FragmentScope
import com.maliks.applocker.xtreme.data.database.callblocker.addtoblacklist.AddToBlackListDialog
import com.maliks.applocker.xtreme.ui.background.BackgroundsFragment
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.BlackListFragment
import com.maliks.applocker.xtreme.ui.callblocker.log.CallLogFragment
import com.maliks.applocker.xtreme.ui.security.SecurityFragment
import com.maliks.applocker.xtreme.ui.settings.SettingsFragment
import com.maliks.applocker.xtreme.ui.vault.vaultlist.VaultListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun securityFragment(): SecurityFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun backgroundFragment(): BackgroundsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun vaultListFragment(): VaultListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun blackListFragment(): BlackListFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun callLogFragment(): CallLogFragment

}