package com.maliks.applocker.xtreme.di.component

import com.maliks.applocker.xtreme.di.module.ActivityBuilderModule
import com.maliks.applocker.xtreme.di.module.AppModule
import com.maliks.applocker.xtreme.AppLockXtremeApplication
import com.maliks.applocker.xtreme.data.database.DatabaseModule
import com.maliks.applocker.xtreme.di.module.BroadcastReceiverBuilderModule
import com.maliks.applocker.xtreme.di.module.ServiceBuilderModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBuilderModule::class,
        ServiceBuilderModule::class,
        BroadcastReceiverBuilderModule::class,
        AppModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent : AndroidInjector<AppLockXtremeApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: AppLockXtremeApplication): Builder

        fun build(): AppComponent
    }
}
