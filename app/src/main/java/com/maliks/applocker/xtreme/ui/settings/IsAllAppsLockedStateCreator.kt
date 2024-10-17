package com.maliks.applocker.xtreme.ui.settings

import android.annotation.SuppressLint
import android.util.Log
import com.maliks.applocker.xtreme.data.AppData
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppEntity
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class IsAllAppsLockedStateCreator : BiFunction<List<AppData>, List<LockedAppEntity>, Boolean> {

    @SuppressLint("LongLogTag")
    override fun apply(installedApps: List<AppData>, lockedApps: List<LockedAppEntity>): Boolean {
        val appPackageName = "com.maliks.applocker.xtreme" // Your app's package name

        val filteredInstalledApps = installedApps.filter { it.packageName != appPackageName }
        val installedPackages = filteredInstalledApps.map { it.packageName }.sorted()
        val lockedPackages = lockedApps.map { it.packageName }.sorted()

        Log.d("IsAllAppsLockedStateCreator", "Filtered installed packages: $installedPackages")
        Log.d("IsAllAppsLockedStateCreator", "Locked packages: $lockedPackages")
        val installedPackages_ = installedPackages.size - 1
        val isAllLocked = installedPackages_  == lockedPackages.size
        Log.d("IsAllAppsLockedStateCreator", "isAllAppsLocked: $isAllLocked")

        return isAllLocked
    }

    companion object {

        fun create(
            appDataListObservable: Observable<List<AppData>>,
            lockedAppsObservable: Observable<List<LockedAppEntity>>
        ): Observable<Boolean> {
            return Observable.combineLatest(
                appDataListObservable,
                lockedAppsObservable,
                IsAllAppsLockedStateCreator()
            )
        }
    }
}
