package com.maliks.applocker.xtreme

import android.content.Context
import androidx.multidex.MultiDex
import com.bugsnag.android.Bugsnag
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.google.android.gms.ads.MobileAds
import com.maliks.applocker.xtreme.service.worker.WorkerStarter
import com.raqun.beaverlib.Beaver
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import com.maliks.applocker.xtreme.di.component.DaggerAppComponent
import com.maliks.applocker.xtreme.service.ServiceStarter

class AppLockXtremeApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder()
            .application(this)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            // You can log or handle the initialization status here if needed
        }
        Stetho.initializeWithDefaults(this)
        Bugsnag.start(this)
        Beaver.build(this)
        ServiceStarter.startService(this)
        SoLoader.init(this, false)
        WorkerStarter.startServiceCheckerWorker()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
