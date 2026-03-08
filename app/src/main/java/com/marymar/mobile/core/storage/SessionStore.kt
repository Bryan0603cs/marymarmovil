package com.marymar.mobile.core.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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
    }

    val sessionFlow: Flow<SessionSnapshot> = context.dataStore.data.map { prefs ->
        SessionSnapshot(
            token = prefs[Keys.TOKEN],
            email = prefs[Keys.EMAIL],
            name = prefs[Keys.NAME],
            role = prefs[Keys.ROLE],
            userId = prefs[Keys.USER_ID],
            loggedIn = prefs[Keys.LOGGED_IN] ?: false
        )
    }

    suspend fun saveSession(
        token: String,
        email: String,
        name: String,
        role: String,
        userId: Long
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.EMAIL] = email
            prefs[Keys.NAME] = name
            prefs[Keys.ROLE] = role
            prefs[Keys.USER_ID] = userId
            prefs[Keys.LOGGED_IN] = true
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

data class SessionSnapshot(
    val token: String?,
    val email: String?,
    val name: String?,
    val role: String?,
    val userId: Long?,
    val loggedIn: Boolean
)
