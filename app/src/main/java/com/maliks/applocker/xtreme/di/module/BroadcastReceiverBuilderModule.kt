package com.maliks.applocker.xtreme.di.module

import com.maliks.applocker.xtreme.ui.callblocker.service.CallReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ContributesAndroidInjector
    abstract fun callBroadcastReceiver(): CallReceiver

}