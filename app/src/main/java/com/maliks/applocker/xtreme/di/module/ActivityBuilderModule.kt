package com.maliks.applocker.xtreme.di.module

import com.maliks.applocker.xtreme.di.scope.ActivityScope
import com.maliks.applocker.xtreme.ui.background.BackgroundsActivity
import com.maliks.applocker.xtreme.ui.browser.BrowserActivity
import com.maliks.applocker.xtreme.ui.callblocker.CallBlockerActivity
import com.maliks.applocker.xtreme.ui.intruders.IntrudersPhotosActivity
import com.maliks.applocker.xtreme.ui.main.MainActivity
import com.maliks.applocker.xtreme.ui.newpattern.CreateNewPatternActivity
import com.maliks.applocker.xtreme.ui.overlay.activity.OverlayValidationActivity
import com.maliks.applocker.xtreme.ui.permissions.PermissionsActivity
import com.maliks.applocker.xtreme.ui.vault.VaultActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by mertsimsek on 12/11/2017.
 */
@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun backgroundActivity(): BackgroundsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun browserActivity(): BrowserActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun vaultActivity(): VaultActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun callBlockerActivity(): CallBlockerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class, DialogFragmentBuilderModule::class])
    abstract fun intrudersPhotosActivity(): IntrudersPhotosActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun permissionsActivity(): PermissionsActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun createNewPatternActivity(): CreateNewPatternActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun fingerPrintOverlayActivity(): OverlayValidationActivity
}