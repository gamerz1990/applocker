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

        // Filtering out the package to avoid the app locker itself being included
        val filteredInstalledApps = installedApps.filter { it.packageName != appPackageName }

        // Calculate the actual installed packages count needed for the comparison
      //  val installedPackagesCount = filteredInstalledApps.size - 1  // Subtract 1 if needed as per your requirement

        // Map and sort the list of filtered installed apps and locked apps by package name
        val distinctInstalledApps = filteredInstalledApps.distinctBy { it.packageName }

        val installedPackages = distinctInstalledApps.map { it.packageName }.sorted()
        val lockedPackages = lockedApps.map { it.packageName }.sorted()

        // Logging the filtered and sorted package names for debugging purposes
        Log.d("IsAllAppsLockedStateCreator", "Filtered installed packages: $installedPackages")
        Log.d("IsAllAppsLockedStateCreator", "Locked packages: $lockedPackages")
        Log.d("IsAllAppsLockedStateCreator", "Installed packages count (adjusted): ${installedPackages.size}")

        // Check if all installed apps are locked
        val isAllLocked = lockedPackages.size == installedPackages.size && installedPackages == lockedPackages
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
