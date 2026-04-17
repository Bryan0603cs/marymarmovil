package com.marymar.mobile.core.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.marymar.mobile.BuildConfig
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GoogleSignInManager @Inject constructor() {

    suspend fun requestGoogleIdToken(activity: Activity): Result<String> {
        return withContext(Dispatchers.Main) {
            runCatching {
                val credentialManager = CredentialManager.create(activity)

                val googleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                )
                    .setNonce(UUID.randomUUID().toString())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                extractIdToken(result)
            }
        }
    }

    fun toUserMessage(error: Throwable): String {
        return when (error) {
            is GetCredentialCancellationException -> "Inicio de sesión cancelado"
            is NoCredentialException -> "No hay cuentas de Google disponibles en este dispositivo"
            is GoogleIdTokenParsingException -> "No fue posible leer la credencial de Google"
            is GetCredentialException -> "No fue posible iniciar sesión con Google"
            else -> error.message ?: "Error inesperado al iniciar sesión con Google"
        }
    }

    private fun extractIdToken(result: GetCredentialResponse): String {
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleCredential.idToken.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Google no devolvió un idToken válido")
        }

        throw IllegalStateException("No se recibió una credencial válida de Google")
    }
}
