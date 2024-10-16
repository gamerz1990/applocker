package com.maliks.applocker.xtreme.ui.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.AppDataProvider
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppEntity
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppsDao
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.ui.security.function.AddSectionHeaderViewStateFunction
import com.maliks.applocker.xtreme.ui.security.function.LockedAppListViewStateCreator
import com.maliks.applocker.xtreme.util.extensions.doOnBackground
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SecurityViewModel @Inject constructor(
    appDataProvider: AppDataProvider,
    val lockedAppsDao: LockedAppsDao
) : RxAwareViewModel() {

    private val appDataViewStateListLiveData = MutableLiveData<List<AppLockItemBaseViewState>>()

    init {
        val installedAppsObservable = appDataProvider.fetchInstalledAppList().toObservable()
        val lockedAppsObservable = lockedAppsDao.getLockedApps().toObservable()

        disposables += LockedAppListViewStateCreator.create(installedAppsObservable, lockedAppsObservable)
            .map(AddSectionHeaderViewStateFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appDataViewStateListLiveData.value = it
            }
    }

    fun getAppDataListLiveData(): LiveData<List<AppLockItemBaseViewState>> = appDataViewStateListLiveData

    fun lockApp(appLockItemViewState: AppLockItemItemViewState) {
        disposables += doOnBackground {
            lockedAppsDao.lockApp(
                LockedAppEntity(
                    appLockItemViewState.appData.packageName
                )
            )
        }
    }

    fun unlockApp(appLockItemViewState: AppLockItemItemViewState) {
        disposables += doOnBackground {
            lockedAppsDao.unlockApp(appLockItemViewState.appData.packageName)
        }
    }
}