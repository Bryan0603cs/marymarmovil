package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.core.storage.SessionSnapshot
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.SoftSecondaryButton


private val ProfileBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val ProfilePrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val ProfileMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val ProfileCard = androidx.compose.ui.graphics.Color.White
private val ProfileField = androidx.compose.ui.graphics.Color(0xFFF1EEE6)

@Composable
fun ProfileScreen(
    session: SessionSnapshot,
    passwordChangeLoading: Boolean,
    passwordChangeInfo: String?,
    passwordChangeError: String?,
    onClearPasswordFeedback: () -> Unit,
    onSaveProfile: (String, String, String, String) -> Unit,
    onRequestPasswordChange: () -> Unit,
    onLogout: () -> Unit
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var draftName by rememberSaveable { mutableStateOf(session.name.orEmpty()) }
    var draftEmail by rememberSaveable { mutableStateOf(session.email.orEmpty()) }
    var draftPhone by rememberSaveable { mutableStateOf(session.phone.orEmpty()) }
    var draftAddress by rememberSaveable { mutableStateOf(session.address.orEmpty()) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var localInfo by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(session.name, session.email, session.phone, session.address) {
        if (!editing) {
            draftName = session.name.orEmpty()
            draftEmail = session.email.orEmpty()
            draftPhone = session.phone.orEmpty()
            draftAddress = session.address.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .background(ProfileBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Perfil")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = ProfileCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editing) {
                    SimpleField(
                        value = draftName,
                        onValueChange = {
                            draftName = it
                            formError = null
                            localInfo = null
                        },
                        label = "Nombre"
                    )

                    SimpleField(
                        value = draftEmail,
                        onValueChange = {
                            draftEmail = it
                            formError = null
                            localInfo = null
                        },
                        label = "Correo",
                        keyboardType = KeyboardType.Email
                    )

                    SimpleField(
                        value = draftPhone,
                        onValueChange = {
                            draftPhone = it
                            formError = null
                            localInfo = null
                        },
                        label = "Teléfono",
                        keyboardType = KeyboardType.Phone
                    )

                    SimpleField(
                        value = draftAddress,
                        onValueChange = {
                            draftAddress = it
                            formError = null
                            localInfo = null
                        },
                        label = "Dirección"
                    )

                    formError?.let { ErrorBanner(it) }
                    localInfo?.let { InfoBanner(it) }

                    DarkPrimaryButton(text = "Guardar") {
                        val cleanName = draftName.trim()
                        val cleanEmail = draftEmail.trim()
                        val cleanPhone = draftPhone.trim()
                        val cleanAddress = draftAddress.trim()

                        when {
                            cleanName.isBlank() -> formError = "El nombre no puede quedar vacío."
                            cleanEmail.isBlank() || !cleanEmail.contains("@") -> formError = "Escribe un correo válido."
                            else -> {
                                onSaveProfile(cleanName, cleanEmail, cleanPhone, cleanAddress)
                                editing = false
                                formError = null
                                localInfo = "Datos actualizados."
                            }
                        }
                    }

                    SoftSecondaryButton(text = "Cancelar") {
                        editing = false
                        draftName = session.name.orEmpty()
                        draftEmail = session.email.orEmpty()
                        draftPhone = session.phone.orEmpty()
                        draftAddress = session.address.orEmpty()
                        formError = null
                        localInfo = null
                    }
                } else {
                    ProfileRow("Nombre", session.name ?: "No disponible")
                    ProfileRow("Correo", session.email ?: "No disponible")
                    if (!session.phone.isNullOrBlank()) ProfileRow("Teléfono", session.phone ?: "")
                    if (!session.address.isNullOrBlank()) ProfileRow("Dirección", session.address ?: "")
                    ProfileRow("Rol", formatRole(session.role))

                    localInfo?.let { InfoBanner(it) }

                    DarkPrimaryButton(text = "Editar datos") {
                        editing = true
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = ProfileCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Seguridad",
                    color = ProfilePrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )

                passwordChangeError?.let { ErrorBanner(it) }
                passwordChangeInfo?.let { InfoBanner(it) }

                DarkPrimaryButton(
                    text = if (passwordChangeLoading) "Enviando..." else "Cambiar contraseña",
                    enabled = !passwordChangeLoading
                ) {
                    onClearPasswordFeedback()
                    onRequestPasswordChange()
                }

                SoftSecondaryButton(text = "Cerrar sesión") {
                    onLogout()
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = ProfileMuted,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            color = ProfilePrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SimpleField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

private fun formatRole(role: String?): String {
    return when (role?.uppercase()) {
        "MESERO" -> "Mesero"
        "ADMINISTRADOR" -> "Administrador"
        "ADMIN" -> "Administrador"
        else -> "Cliente"
    }
}