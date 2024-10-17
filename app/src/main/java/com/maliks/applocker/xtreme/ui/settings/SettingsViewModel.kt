package com.maliks.applocker.xtreme.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.AppDataProvider
import com.maliks.applocker.xtreme.data.AppLockerPreferences
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppEntity
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppsDao
import com.maliks.applocker.xtreme.ui.RxAwareAndroidViewModel
import com.maliks.applocker.xtreme.util.extensions.doOnBackground
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val app: Application,
    val appDataProvider: AppDataProvider,
    val lockedAppsDao: LockedAppsDao,
    val appLockerPreferences: AppLockerPreferences
) : RxAwareAndroidViewModel(app) {

    private val _settingsViewStateLiveData = MutableLiveData<SettingsViewState>()
    val settingsViewStateLiveData: LiveData<SettingsViewState> get() = _settingsViewStateLiveData

    private val _fingerPrintStatusViewStateLiveData = MutableLiveData<FingerPrintStatusViewState>()
    val fingerPrintStatusViewStateLiveData: LiveData<FingerPrintStatusViewState> get() = _fingerPrintStatusViewStateLiveData

    init {
        _settingsViewStateLiveData.value = SettingsViewState(
            isHiddenDrawingMode = appLockerPreferences.getHiddenDrawingMode(),
            isFingerPrintEnabled = appLockerPreferences.getFingerPrintEnabled(),
            isIntrudersCatcherEnabled = appLockerPreferences.getIntrudersCatcherEnabled()
        )

        with(FingerprintIdentify(app)) {
            init()
            _fingerPrintStatusViewStateLiveData.value = FingerPrintStatusViewState(
                isFingerPrintSupported = isHardwareEnable,
                isFingerPrintRegistered = isRegisteredFingerprint
            )
        }

        val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
        val lockedAppsObservable = lockedAppsDao.getLockedApps().toObservable()

        disposables += IsAllAppsLockedStateCreator.create(
            installedAppsObservable,
            lockedAppsObservable
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isAllAppsLocked ->
                val currentViewState = _settingsViewStateLiveData.value ?: SettingsViewState()
                _settingsViewStateLiveData.value = currentViewState.copy(
                    isAllAppLocked = isAllAppsLocked
                )
            }
    }

    fun isAllLocked() = settingsViewStateLiveData.value?.isAllAppLocked ?: false

    fun isIntrudersCatcherEnabled() = settingsViewStateLiveData.value?.isIntrudersCatcherEnabled ?: false

    fun lockAll() {
        disposables += appDataProvider
            .fetchInstalledAppList()
            .map {
                val entityList: ArrayList<LockedAppEntity> = arrayListOf()
                it.forEach { appData ->
                    entityList.add(appData.toEntity())
                }
                lockedAppsDao.lockApps(entityList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun unlockAll() {
        disposables += doOnBackground {
            lockedAppsDao.unlockAll()
        }
    }

    fun setHiddenDrawingMode(hiddenDrawingMode: Boolean) {
        appLockerPreferences.setHiddenDrawingMode(hiddenDrawingMode)
        val currentViewState = _settingsViewStateLiveData.value ?: SettingsViewState()
        _settingsViewStateLiveData.value = currentViewState.copy(
            isHiddenDrawingMode = hiddenDrawingMode
        )
    }

    fun setEnableFingerPrint(fingerPrintEnabled: Boolean) {
        appLockerPreferences.setFingerPrintEnable(fingerPrintEnabled)
        val currentViewState = _settingsViewStateLiveData.value ?: SettingsViewState()
        _settingsViewStateLiveData.value = currentViewState.copy(
            isFingerPrintEnabled = fingerPrintEnabled
        )
    }

    fun setEnableIntrudersCatchers(intruderCatcherEnabled: Boolean) {
        appLockerPreferences.setIntrudersCatcherEnable(intruderCatcherEnabled)
        val currentViewState = _settingsViewStateLiveData.value ?: SettingsViewState()
        _settingsViewStateLiveData.value = currentViewState.copy(
            isIntrudersCatcherEnabled = intruderCatcherEnabled
        )
    }

    fun onLockAllAppsClicked() {
        if (isAllLocked()) {
            unlockAll()
        } else {
            lockAll()
        }
    }
}
