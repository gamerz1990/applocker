package com.maliks.applocker.xtreme.service.stateprovider

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.app.ActivityManager
import android.app.usage.UsageStatsManager.INTERVAL_DAILY
import androidx.annotation.RequiresApi
import com.maliks.applocker.xtreme.ui.overlay.activity.OverlayValidationActivity

class AppForegroundObservable @Inject constructor(val context: Context) {

    private var foregroundFlowable: Flowable<String>? = null

    fun get(): Flowable<String> {
        foregroundFlowable = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> getForegroundObservableHigherLollipop()
            else -> getForegroundObservableLowerLollipop()
        }

        return foregroundFlowable!!
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getForegroundObservableHigherLollipop(): Flowable<String> {
            return Flowable.interval(250, TimeUnit.MILLISECONDS)
                .filter { PermissionChecker.checkUsageAccessPermission(context) }
                .map {
                    val mUsageStatsManager =
                        context.getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
                    val time = System.currentTimeMillis()

                    val usageEvents =
                        mUsageStatsManager.queryUsageStats(INTERVAL_DAILY, time - 1000 * 10, time)
                    val sortedUsageEvents = usageEvents.sortedBy { it.lastTimeUsed }
                    val usageEvent = sortedUsageEvents.lastOrNull()

                    UsageEventWrapper(usageEvent)
                }
                .filter { it.usageEvent != null }
                .map { it.usageEvent }
                .filter { it.packageName != null }
                .filter {
                    it.packageName.contains(OverlayValidationActivity::class.java.simpleName).not()
                }
                .map { it.packageName }
                .distinctUntilChanged()
        }


        private fun getForegroundObservableLowerLollipop(): Flowable<String> {
        return Flowable.interval(100, TimeUnit.MILLISECONDS)
            .map {
                val mActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                mActivityManager.getRunningTasks(1)[0].topActivity
            }
            .filter { it.className.contains(OverlayValidationActivity::class.java.simpleName).not() }
            .map { it.packageName }
            .distinctUntilChanged()
    }
}