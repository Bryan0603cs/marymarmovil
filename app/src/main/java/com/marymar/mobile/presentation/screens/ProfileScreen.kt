package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite

@Composable
fun ProfileScreen(
    session: SessionSnapshot,
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
    var passwordInfo by rememberSaveable { mutableStateOf<String?>(null) }

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
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(title = "Mi perfil")

        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editing) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = {
                            draftName = it
                            formError = null
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
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Dirección") }
                    )

                    if (formError != null) {
                        ErrorBanner(formError ?: "")
                    }

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
                            },
                            modifier = Modifier.weight(1f)
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
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Guardar")
                        }
                    }
                } else {
                    ProfileRow(label = "Nombre", value = session.name ?: "No disponible")
                    ProfileRow(label = "Correo", value = session.email ?: "No disponible")
                    ProfileRow(label = "Teléfono", value = session.phone ?: "No disponible")
                    ProfileRow(label = "Dirección", value = session.address ?: "No disponible")

                    OutlinedButton(
                        onClick = { editing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Editar información")
                    }
                }
            }
        }

        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Seguridad",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Para cambiar tu contraseña se enviará el proceso de restablecimiento al correo actual del usuario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )

                OutlinedButton(
                    onClick = {
                        onRequestPasswordChange()
                        passwordInfo = "Se envió el proceso de cambio de contraseña a tu correo."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cambiar contraseña")
                }

                passwordInfo?.let {
                    InfoBanner(it)
                }
            }
        }

        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileRow(label = "Rol", value = session.role ?: "No disponible")
                ProfileRow(label = "ID", value = session.userId?.toString() ?: "No disponible")
                ProfileRow(label = "Identificación", value = session.idNumber ?: "No disponible")
                ProfileRow(label = "Fecha de nacimiento", value = session.birthDate ?: "No disponible")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryActionButton(
            text = "Cerrar sesión",
            onClick = onLogout
        )
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}