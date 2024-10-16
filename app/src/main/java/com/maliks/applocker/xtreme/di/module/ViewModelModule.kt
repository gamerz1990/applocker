package com.maliks.applocker.xtreme.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maliks.applocker.xtreme.data.database.callblocker.addtoblacklist.AddToBlackListViewModel
import com.maliks.applocker.xtreme.di.ViewModelFactory
import com.maliks.applocker.xtreme.di.key.ViewModelKey
import com.maliks.applocker.xtreme.ui.background.BackgroundsActivityViewModel
import com.maliks.applocker.xtreme.ui.background.BackgroundsFragmentViewModel
import com.maliks.applocker.xtreme.ui.browser.BrowserViewModel
import com.maliks.applocker.xtreme.ui.browser.bookmarks.BookmarksViewModel
import com.maliks.applocker.xtreme.ui.callblocker.CallBlockerViewModel
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.BlackListFragment
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.BlackListViewModel
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.delete.BlackListItemDeleteViewModel
import com.maliks.applocker.xtreme.ui.callblocker.log.CallLogViewModel
import com.maliks.applocker.xtreme.ui.intruders.IntrudersPhotosViewModel
import com.maliks.applocker.xtreme.ui.main.MainViewModel
import com.maliks.applocker.xtreme.ui.newpattern.CreateNewPatternViewModel
import com.maliks.applocker.xtreme.ui.overlay.activity.OverlayValidationViewModel
import com.maliks.applocker.xtreme.ui.permissiondialog.UsageAccessPermissionViewModel
import com.maliks.applocker.xtreme.ui.permissions.PermissionsViewModel
import com.maliks.applocker.xtreme.ui.policydialog.PrivacyPolicyViewModel
import com.maliks.applocker.xtreme.ui.rateus.RateUsViewModel
import com.maliks.applocker.xtreme.ui.security.SecurityViewModel
import com.maliks.applocker.xtreme.ui.settings.SettingsViewModel
import com.maliks.applocker.xtreme.ui.vault.VaultViewModel
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.AddToVaultViewModel
import com.maliks.applocker.xtreme.ui.vault.removingvaultdialog.RemoveFromVaultViewModel
import com.maliks.applocker.xtreme.ui.vault.vaultlist.VaultListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @IntoMap
    @Binds
    @ViewModelKey(OverlayValidationViewModel::class)
    abstract fun provideFingerPrintOverlayViewModel(overlayValidationViewModel: OverlayValidationViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(CreateNewPatternViewModel::class)
    abstract fun provideCreateNewPatternViewModel(createNewPatternViewModel: CreateNewPatternViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PermissionsViewModel::class)
    abstract fun providePermissionsViewModel(permissionsViewModel: PermissionsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(MainViewModel::class)
    abstract fun provideMainVieWModel(mainViewModel: MainViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BackgroundsActivityViewModel::class)
    abstract fun provideBackgroundActivityViewModel(backgroundsActivityViewModel: BackgroundsActivityViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SecurityViewModel::class)
    abstract fun provideSecurityVieWModel(securityViewModel: SecurityViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BrowserViewModel::class)
    abstract fun provideBrowserViewModel(browserViewModel: BrowserViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BackgroundsFragmentViewModel::class)
    abstract fun provideBackgroundViewModel(backgroundsFragmentViewModel: BackgroundsFragmentViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(SettingsViewModel::class)
    abstract fun provideSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(RateUsViewModel::class)
    abstract fun provideRateUsViewModel(rateUsViewModel: RateUsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(UsageAccessPermissionViewModel::class)
    abstract fun provideUsageAccessPermissionViewModel(permissionsViewModel: PermissionsViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(PrivacyPolicyViewModel::class)
    abstract fun providePrivacyPolicyViewModel(privacyPolicyViewModel: PrivacyPolicyViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BookmarksViewModel::class)
    abstract fun provideBookmarksViewModel(bookmarksViewModel: BookmarksViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(VaultViewModel::class)
    abstract fun provideVaultViewModel(vaultViewModel: VaultViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(VaultListViewModel::class)
    abstract fun provideVaultListViewModel(vaultListViewModel: VaultListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(AddToVaultViewModel::class)
    abstract fun provideAddToVaultViewModel(addToVaultViewModel: AddToVaultViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(RemoveFromVaultViewModel::class)
    abstract fun provideRemoveFromVaultViewModel(removeFromVaultViewModel: RemoveFromVaultViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(CallBlockerViewModel::class)
    abstract fun provideCallBlockerViewModel(callBlockerViewModel: CallBlockerViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BlackListViewModel::class)
    abstract fun provideBlackListViewModel(blackListViewModel: BlackListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(CallLogViewModel::class)
    abstract fun provideCallLogViewModel(callLogViewModel: CallLogViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(AddToBlackListViewModel::class)
    abstract fun provideAddToBlackListViewModel(addToBlackListViewModel: AddToBlackListViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(BlackListItemDeleteViewModel::class)
    abstract fun provideDeleteBlackListItemViewModel(blackListItemDeleteViewModel: BlackListItemDeleteViewModel): ViewModel

    @IntoMap
    @Binds
    @ViewModelKey(IntrudersPhotosViewModel::class)
    abstract fun provideIntrudersPhotosViewModel(intrudersPhotosViewModel: IntrudersPhotosViewModel): ViewModel

    @Binds
    abstract fun provideViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}

