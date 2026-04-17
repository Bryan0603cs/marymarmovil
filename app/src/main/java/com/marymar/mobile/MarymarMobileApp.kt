package com.marymar.mobile

import android.app.Application
import com.marymar.mobile.core.security.NativeRecaptchaManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MarymarMobileApp : Application() {

    @Inject
    lateinit var nativeRecaptchaManager: NativeRecaptchaManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            nativeRecaptchaManager.warmUp()
        }
    }
}