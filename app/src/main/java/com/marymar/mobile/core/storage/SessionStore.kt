package com.marymar.mobile.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_store")

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("name")
        val ROLE = stringPreferencesKey("role")
        val USER_ID = longPreferencesKey("user_id")
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val PHONE = stringPreferencesKey("phone")
        val ADDRESS = stringPreferencesKey("address")
        val BIRTH_DATE = stringPreferencesKey("birth_date")
        val ID_NUMBER = stringPreferencesKey("id_number")
    }

    val sessionFlow: Flow<SessionSnapshot> = context.dataStore.data.map { prefs ->
        SessionSnapshot(
            token = prefs[Keys.TOKEN],
            email = prefs[Keys.EMAIL],
            name = prefs[Keys.NAME],
            role = prefs[Keys.ROLE],
            userId = prefs[Keys.USER_ID],
            loggedIn = prefs[Keys.LOGGED_IN] ?: false,
            phone = prefs[Keys.PHONE],
            address = prefs[Keys.ADDRESS],
            birthDate = prefs[Keys.BIRTH_DATE],
            idNumber = prefs[Keys.ID_NUMBER]
        )
    }

    suspend fun saveSession(
        token: String,
        email: String,
        name: String,
        role: String,
        userId: Long,
        phone: String? = null,
        address: String? = null,
        birthDate: String? = null,
        idNumber: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.EMAIL] = email
            prefs[Keys.NAME] = name
            prefs[Keys.ROLE] = role
            prefs[Keys.USER_ID] = userId
            prefs[Keys.LOGGED_IN] = true

            val resolvedPhone = phone.toUsefulValue() ?: prefs[Keys.PHONE]
            val resolvedAddress = address.toUsefulValue() ?: prefs[Keys.ADDRESS]
            val resolvedBirthDate = birthDate.toUsefulValue() ?: prefs[Keys.BIRTH_DATE]
            val resolvedIdNumber = idNumber.toUsefulValue() ?: prefs[Keys.ID_NUMBER]

            if (resolvedPhone != null) prefs[Keys.PHONE] = resolvedPhone else prefs.remove(Keys.PHONE)
            if (resolvedAddress != null) prefs[Keys.ADDRESS] = resolvedAddress else prefs.remove(Keys.ADDRESS)
            if (resolvedBirthDate != null) prefs[Keys.BIRTH_DATE] = resolvedBirthDate else prefs.remove(Keys.BIRTH_DATE)
            if (resolvedIdNumber != null) prefs[Keys.ID_NUMBER] = resolvedIdNumber else prefs.remove(Keys.ID_NUMBER)
        }
    }

    suspend fun updateProfile(
        name: String,
        email: String,
        phone: String,
        address: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NAME] = name.trim()
            prefs[Keys.EMAIL] = email.trim()
            prefs[Keys.PHONE] = phone.trim()
            prefs[Keys.ADDRESS] = address.trim()
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun String?.toUsefulValue(): String? {
        val cleaned = this?.trim()
        return cleaned?.takeIf { it.isNotEmpty() }
    }
}

data class SessionSnapshot(
    val token: String?,
    val email: String?,
    val name: String?,
    val role: String?,
    val userId: Long?,
    val loggedIn: Boolean,
    val phone: String?,
    val address: String?,
    val birthDate: String?,
    val idNumber: String?
)