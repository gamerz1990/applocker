package com.maliks.applocker.xtreme.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.andrognito.patternlockview.PatternLockView
import com.bugsnag.android.App
import com.bugsnag.android.Bugsnag
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.AppLockerPreferences
import com.maliks.applocker.xtreme.data.SystemPackages
import com.maliks.applocker.xtreme.data.database.lockedapps.LockedAppsDao
import com.maliks.applocker.xtreme.data.database.pattern.PatternDao
import com.maliks.applocker.xtreme.data.database.pattern.PatternDot
import com.maliks.applocker.xtreme.service.notification.ServiceNotificationManager
import com.maliks.applocker.xtreme.service.stateprovider.AppForegroundObservable
import com.maliks.applocker.xtreme.service.stateprovider.PermissionCheckerObservable
import com.maliks.applocker.xtreme.ui.NotificationPermissionActivity
import com.maliks.applocker.xtreme.ui.overlay.activity.OverlayValidationActivity
import com.maliks.applocker.xtreme.ui.overlay.view.OverlayViewLayoutParams
import com.maliks.applocker.xtreme.ui.overlay.view.PatternOverlayView
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker
import com.maliks.applocker.xtreme.util.AdManager
import com.maliks.applocker.xtreme.util.extensions.convertToPatternDot
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import dagger.android.DaggerService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AppLockerService : DaggerService() {

    @Inject
    lateinit var serviceNotificationManager: ServiceNotificationManager

    @Inject
    lateinit var appForegroundObservable: AppForegroundObservable

    @Inject
    lateinit var permissionCheckerObservable: PermissionCheckerObservable

    @Inject
    lateinit var lockedAppsDao: LockedAppsDao

    @Inject
    lateinit var patternDao: PatternDao

    @Inject
    lateinit var appLockerPreferences: AppLockerPreferences

    private val validatedPatternObservable = PublishSubject.create<List<PatternDot>>()
    private val allDisposables: CompositeDisposable = CompositeDisposable()
    private var foregroundAppDisposable: Disposable? = null
    private val lockedAppPackageSet: HashSet<String> = HashSet()
    private lateinit var windowManager: WindowManager
    private lateinit var overlayParams: WindowManager.LayoutParams
    private lateinit var overlayView: PatternOverlayView
    private var isOverlayShowing = false
    private var lastForegroundAppPackage: String? = null

    private val screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> observeForegroundApplication()
                Intent.ACTION_SCREEN_OFF -> stopForegroundApplicationObserver()
            }
        }
    }

    private val installUninstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Implementation for handling app install/uninstall
        }
    }

    init {
        SystemPackages.getSystemPackages().forEach { lockedAppPackageSet.add(it) }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        checkAndRequestNotificationPermission();

        initializeAppLockerNotification()
        initializeOverlayView()
        registerScreenReceiver()
        registerInstallUninstallReceiver()
        observeLockedApps()
        observeOverlayView()
        observeForegroundApplication()
        observePermissionChecker()
    }


    @SuppressLint("InlinedApi")
    private fun checkAndRequestNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, launch an activity to request it
            val intent = Intent(applicationContext, NotificationPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
   override fun onDestroy() {
        Handler(Looper.getMainLooper()).postDelayed({
            ServiceStarter.startService(applicationContext)
        }, 1000) // Delay restart by 1 second

        unregisterScreenReceiver()
        unregisterInstallUninstallReceiver()
        if (!allDisposables.isDisposed) {
            allDisposables.dispose()
        }
        super.onDestroy()
    }


    private fun registerInstallUninstallReceiver() {
        val installUninstallFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_INSTALL)
            addDataScheme("package")
        }
        registerReceiver(installUninstallReceiver, installUninstallFilter)
    }

    private fun unregisterInstallUninstallReceiver() {
        unregisterReceiver(installUninstallReceiver)
    }

    private fun registerScreenReceiver() {
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenOnOffReceiver, screenFilter)
    }

    private fun unregisterScreenReceiver() {
        unregisterReceiver(screenOnOffReceiver)
    }

    private fun observeLockedApps() {
        allDisposables += lockedAppsDao.getLockedApps()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lockedAppList ->
                    lockedAppPackageSet.clear()
                    lockedAppList.forEach { lockedAppPackageSet.add(it.parsePackageName()) }
                    SystemPackages.getSystemPackages().forEach { lockedAppPackageSet.add(it) }
                },
                { error -> Bugsnag.notify(error) }
            )
    }

    private fun observeOverlayView() {
        allDisposables += Flowable
            .combineLatest(
                patternDao.getPattern().map { it.patternMetadata.pattern },
                validatedPatternObservable.toFlowable(BackpressureStrategy.BUFFER),
                PatternValidatorFunction()
            )
            .subscribe(this::onPatternValidated)
    }

    private fun initializeOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayParams = OverlayViewLayoutParams.get()
        overlayView = PatternOverlayView(applicationContext).apply {
            observePattern(this@AppLockerService::onDrawPattern)
        }
    }

    private fun observeForegroundApplication() {
        if (foregroundAppDisposable != null && foregroundAppDisposable?.isDisposed == false) {
            Log.d("AppLockerService", "Foreground application observer already running.")
            return
        }

        Log.d("AppLockerService", "Starting foreground application observer.")
        foregroundAppDisposable = appForegroundObservable
            .get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { foregroundAppPackage -> onAppForeground(foregroundAppPackage) },
                { error -> Bugsnag.notify(error) }
            )
        allDisposables.add(foregroundAppDisposable!!)
    }

    private fun stopForegroundApplicationObserver() {
        if (foregroundAppDisposable != null && foregroundAppDisposable?.isDisposed == false) {
            foregroundAppDisposable?.dispose()
        }
    }

    private fun observePermissionChecker() {
        allDisposables += permissionCheckerObservable
            .get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isPermissionNeeded ->
                if (isPermissionNeeded) {
                    showPermissionNeedNotification()
                } else {
                    serviceNotificationManager.hidePermissionNotification()
                }
            }
    }

    private fun onAppForeground(foregroundAppPackage: String) {
        if (lastForegroundAppPackage == foregroundAppPackage) {
            return
        }

        hideOverlay()
        if (lockedAppPackageSet.contains(foregroundAppPackage)) {
            if (appLockerPreferences.getFingerPrintEnabled() || PermissionChecker.checkOverlayPermission(
                    applicationContext
                ).not()
            ) {
                val intent =
                    OverlayValidationActivity.newIntent(applicationContext, foregroundAppPackage)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                showOverlay(foregroundAppPackage)
            }
        }
        lastForegroundAppPackage = foregroundAppPackage
    }

    private fun onDrawPattern(pattern: List<PatternLockView.Dot>) {
        validatedPatternObservable.onNext(pattern.convertToPatternDot())
    }

    private fun onPatternValidated(isPatternCorrect: Boolean) {
        if (isPatternCorrect) {
            overlayView.notifyDrawnCorrect()
            hideOverlay();
        } else {
            overlayView.notifyDrawnWrong()
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun initializeAppLockerNotification() {
        val notification = serviceNotificationManager.createNotification()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID_APPLOCKER_SERVICE, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID_APPLOCKER_SERVICE, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID_APPLOCKER_SERVICE, notification)
        }
    }

    private fun showPermissionNeedNotification() {
        val notification = serviceNotificationManager.createPermissionNeedNotification()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID_APPLOCKER_PERMISSION_NEED, notification)
    }

    private fun showOverlay(lockedAppPackageName: String) {
        if (!isOverlayShowing) {
            isOverlayShowing = true
            overlayView.setHiddenDrawingMode(appLockerPreferences.getHiddenDrawingMode())
            overlayView.setAppPackageName(lockedAppPackageName)
            windowManager.addView(overlayView, overlayParams)
        }
    }

    private fun hideOverlay() {
        if (isOverlayShowing) {
            isOverlayShowing = false
            windowManager.removeViewImmediate(overlayView)
        }
    }

    companion object {
        private const val NOTIFICATION_ID_APPLOCKER_SERVICE = 1
        private const val NOTIFICATION_ID_APPLOCKER_PERMISSION_NEED = 2
    }
}
