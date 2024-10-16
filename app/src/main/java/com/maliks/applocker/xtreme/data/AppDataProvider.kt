package com.maliks.applocker.xtreme.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(mainIntent, 0)
            val appDataList: ArrayList<AppData> = arrayListOf()

            resolveInfoList.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName

                // Get ApplicationInfo to check the app's flags
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)

                // Filter out system apps except for the ones updated by the user
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {

                    if (packageName != context.packageName) {
                        val appData = AppData(
                            appName = resolveInfo.loadLabel(context.packageManager).toString(),
                            packageName = packageName,
                            appIconDrawable = resolveInfo.loadIcon(context.packageManager)
                        )
                        appDataList.add(appData)
                    }
                }
            }

            val lockedAppList = appsDao.getLockedAppsSync()

            val orderedList = orderAppsByLockStatus(appDataList, lockedAppList)

            emitter.onSuccess(orderedList)
        }
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