package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.marymar.mobile.core.storage.SessionSnapshot
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.BorderGray
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite
import com.marymar.mobile.ui.theme.TextDark

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
            .background(SoftBeige)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(title = "Mi perfil")

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editing) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = {
                            draftName = it
                            formError = null
                            localInfo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Nombre") }
                    )

                    OutlinedTextField(
                        value = draftEmail,
                        onValueChange = {
                            draftEmail = it
                            formError = null
                            localInfo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        label = { Text("Correo") }
                    )

                    OutlinedTextField(
                        value = draftPhone,
                        onValueChange = {
                            draftPhone = it
                            formError = null
                            localInfo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        label = { Text("Teléfono") }
                    )

                    OutlinedTextField(
                        value = draftAddress,
                        onValueChange = {
                            draftAddress = it
                            formError = null
                            localInfo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Dirección") }
                    )

                    formError?.let { ErrorBanner(it) }
                    localInfo?.let { InfoBanner(it) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editing = false
                                draftName = session.name.orEmpty()
                                draftEmail = session.email.orEmpty()
                                draftPhone = session.phone.orEmpty()
                                draftAddress = session.address.orEmpty()
                                formError = null
                                localInfo = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                val cleanName = draftName.trim()
                                val cleanEmail = draftEmail.trim()
                                val cleanPhone = draftPhone.trim()
                                val cleanAddress = draftAddress.trim()

                                when {
                                    cleanName.isBlank() -> {
                                        formError = "El nombre no puede quedar vacío."
                                    }

                                    cleanEmail.isBlank() || !cleanEmail.contains("@") -> {
                                        formError = "Escribe un correo válido."
                                    }

                                    else -> {
                                        onSaveProfile(
                                            cleanName,
                                            cleanEmail,
                                            cleanPhone,
                                            cleanAddress
                                        )
                                        editing = false
                                        formError = null
                                        localInfo = "Datos actualizados"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                        ) {
                            Text("Guardar")
                        }
                    }
                } else {
                    ProfileValueCard(label = "Nombre", value = session.name ?: "No disponible")
                    ProfileValueCard(label = "Correo", value = session.email ?: "No disponible")
                    ProfileValueCard(label = "Teléfono", value = session.phone ?: "No disponible")
                    ProfileValueCard(label = "Dirección", value = session.address ?: "No disponible")
                    ProfileValueCard(label = "Rol", value = formatRole(session.role))
                    ProfileValueCard(label = "ID", value = session.userId?.toString() ?: "No disponible")
                    ProfileValueCard(label = "Identificación", value = session.idNumber ?: "No disponible")

                    localInfo?.let { InfoBanner(it) }

                    OutlinedButton(
                        onClick = {
                            editing = true
                            localInfo = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Editar información")
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryActionButton(
                    text = "Cambiar contraseña",
                    loading = passwordChangeLoading,
                    enabled = !session.email.isNullOrBlank(),
                    onClick = {
                        onClearPasswordFeedback()
                        onRequestPasswordChange()
                    }
                )

                passwordChangeError?.takeIf { it.isNotBlank() }?.let {
                    ErrorBanner(it)
                }

                passwordChangeInfo?.takeIf { it.isNotBlank() }?.let {
                    InfoBanner(it)
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun ProfileValueCard(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MutedText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextDark
            )
        }
    }
}

private fun formatRole(role: String?): String {
    if (role.isNullOrBlank()) return "No disponible"

    return role
        .lowercase()
        .split("_")
        .joinToString(" ") { part ->
            part.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }
}