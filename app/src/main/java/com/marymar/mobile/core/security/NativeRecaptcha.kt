package com.marymar.mobile.core.security

import android.app.Application
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaClient
import com.marymar.mobile.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class NativeRecaptchaManager @Inject constructor(
    private val application: Application
) {

    private val clientLock = Mutex()

    @Volatile
    private var client: RecaptchaClient? = null

    suspend fun warmUp() {
        runCatching { getClient() }
    }

    suspend fun execute(
        actionName: String,
        timeoutMs: Long = 10_000L
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val recaptchaClient = getClient()
                recaptchaClient.execute(
                    RecaptchaAction.custom(actionName),
                    timeout = timeoutMs
                ).getOrElse { throw it }
            }
        }
    }

    private suspend fun getClient(): RecaptchaClient {
        client?.let { return it }

        return clientLock.withLock {
            client?.let { return it }

            val created = Recaptcha.fetchClient(
                application = application,
                siteKey = BuildConfig.RECAPTCHA_ANDROID_SITE_KEY
            )

            client = created
            created
        }
    }
}