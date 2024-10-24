package com.maliks.applocker.xtreme.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppEntity
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppsDao
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class AppDataProvider @Inject constructor(
    val context: Context,
    val appsDao: LockedAppsDao
) {

    fun fetchInstalledAppList(): Single<List<AppData>> {
        return Single.create { emitter ->
            try {
                // Get the list of launchable applications
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfoList = context.packageManager.queryIntentActivities(mainIntent, 0)
                val appDataList: ArrayList<AppData> = arrayListOf()

                // Iterate through the list to create AppData objects and filter as needed
                resolveInfoList.forEach { resolveInfo ->
                    val packageName = resolveInfo.activityInfo.packageName

                    // Get ApplicationInfo to check the app's flags
                    val appInfo = context.packageManager.getApplicationInfo(packageName, 0)

                    // Filter out system apps except the ones updated by the user
                    if (shouldIncludeApp(appInfo, packageName)) {
                        val appData = createAppData(resolveInfo)
                        appDataList.add(appData)
                    }
                }

                // Fetch locked apps and sort the final list by lock status
                val lockedAppList = appsDao.getLockedAppsSync()
                val orderedList = orderAppsByLockStatus(appDataList, lockedAppList)

                // Emit the final ordered list
                emitter.onSuccess(orderedList)

            } catch (e: Exception) {
                // Emit an error if anything goes wrong
                emitter.onError(e)
            }
        }
    }

    /**
     * Helper method to determine if the app should be included in the list.
     */
    private fun shouldIncludeApp(appInfo: ApplicationInfo, packageName: String): Boolean {
        // Exclude system apps except those updated by the user
        val isNonSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        val isNotOwnApp = packageName != context.packageName

        return (isNonSystemApp || isUpdatedSystemApp) && isNotOwnApp
    }

    /**
     * Helper method to create AppData from ResolveInfo.
     */
    private fun createAppData(resolveInfo: ResolveInfo): AppData {
        return AppData(
            appName = resolveInfo.loadLabel(context.packageManager).toString(),
            packageName = resolveInfo.activityInfo.packageName,
            appIconDrawable = resolveInfo.loadIcon(context.packageManager)
        )
    }



    private fun orderAppsByLockStatus(allApps: List<AppData>, lockedApps: List<LockedAppEntity>): List<AppData> {
        val resultList = arrayListOf<AppData>()


        lockedApps.forEach { lockedAppEntity ->
            allApps.forEach { appData ->
                if (lockedAppEntity.packageName == appData.packageName) {
                    resultList.add(appData)
                }
            }
        }


        val alphabeticOrderList: ArrayList<AppData> = arrayListOf()

        allApps.forEach { appData ->
            if (resultList.contains(appData).not()) {
                alphabeticOrderList.add(appData)
            }
        }
        alphabeticOrderList.sortWith(Comparator { app1, app2 -> app1.appName.compareTo(app2.appName) })
        resultList.addAll(alphabeticOrderList)

        return resultList
    }

}