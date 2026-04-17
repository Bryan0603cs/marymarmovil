package com.marymar.mobile.presentation.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.marymar.mobile.R
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SecondaryBlue
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite
import java.util.Calendar

private const val REGISTER_SUPPORT_PHONE = "573003710163"
private const val REGISTER_SUPPORT_EMAIL = "soporte@marymar.com"
private const val REGISTER_SUPPORT_WHATSAPP = "573003710163"
private const val PRIVACY_POLICY_URL = "https://d3hmyhthxmr5gy.cloudfront.net/politica"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val currentDensity = LocalDensity.current

    var idNumber by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var birthDateDisplay by rememberSaveable { mutableStateOf("") }
    var birthDateIso by rememberSaveable { mutableStateOf("") }

    var aceptaDatos by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var showAccessibilitySheet by rememberSaveable { mutableStateOf(false) }
    var showSupportSheet by rememberSaveable { mutableStateOf(false) }

    var fontScale by rememberSaveable { mutableStateOf(1f) }
    var highContrast by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val supportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accessibilitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = if (highContrast) Color(0xFF14181C) else SoftBeige
    val cardColor = if (highContrast) Color(0xFFF6F6F6) else SurfaceWhite
    val headerColor = if (highContrast) Color(0xFF1D242B) else SurfaceWhite
    val primaryText = if (highContrast) Color(0xFF0E3445) else PrimaryBlue
    val secondaryText = if (highContrast) Color(0xFF313131) else MutedText
    val habeasError = state.error?.contains("habeas", ignoreCase = true) == true ||
            state.error?.contains("tratamiento de datos", ignoreCase = true) == true

    val nonSpecialError = state.error?.takeIf {
        !it.contains("habeas", ignoreCase = true) &&
                !it.contains("tratamiento de datos", ignoreCase = true)
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val safeMonth = month + 1
            birthDateDisplay = "%02d/%02d/%04d".format(dayOfMonth, safeMonth, year)
            birthDateIso = "%04d-%02d-%02d".format(year, safeMonth, dayOfMonth)
        },
        calendar.get(Calendar.YEAR) - 18,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScale
        )
    ) {
        if (showSupportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSupportSheet = false },
                sheetState = supportSheetState,
                containerColor = SurfaceWhite
            ) {
                RegisterSupportSheet(
                    onWhatsApp = {
                        val uri = Uri.parse("https://wa.me/$REGISTER_SUPPORT_WHATSAPP")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    onCall = {
                        val uri = Uri.parse("tel:$REGISTER_SUPPORT_PHONE")
                        context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                    },
                    onEmail = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$REGISTER_SUPPORT_EMAIL")
                            putExtra(Intent.EXTRA_SUBJECT, "Soporte Mar y Mar")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        if (showAccessibilitySheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccessibilitySheet = false },
                sheetState = accessibilitySheetState,
                containerColor = SurfaceWhite
            ) {
                RegisterAccessibilitySheet(
                    fontScale = fontScale,
                    highContrast = highContrast,
                    onIncrease = { fontScale = (fontScale + 0.1f).coerceAtMost(1.35f) },
                    onDecrease = { fontScale = (fontScale - 0.1f).coerceAtLeast(0.90f) },
                    onToggleHighContrast = { highContrast = !highContrast },
                    onReset = {
                        fontScale = 1f
                        highContrast = false
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = headerColor,
                    tonalElevation = 1.dp,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mar_y_mar),
                            contentDescription = "Logo Mar y Mar",
                            modifier = Modifier.size(44.dp)
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Column {
                            Text(
                                text = "Mar y Mar",
                                style = MaterialTheme.typography.headlineSmall,
                                color = primaryText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Crea tu cuenta",
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryText
                            )
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Crear cuenta",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = primaryText,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Completa los datos para registrarte",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = {
                                idNumber = it
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Identificación", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Nombre completo", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Correo electrónico", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            placeholder = { Text("Contraseña", color = MutedText) },
                            trailingIcon = {
                                TextButton(onClick = { showPassword = !showPassword }) {
                                    Text(
                                        text = if (showPassword) "Ocultar" else "Mostrar",
                                        color = secondaryText
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = birthDateDisplay,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Fecha de nacimiento", color = MutedText) },
                            trailingIcon = {
                                TextButton(onClick = { datePickerDialog.show() }) {
                                    Text("📅")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("Teléfono", color = MutedText) }
                        )

                        PolicyRow(
                            checked = aceptaDatos,
                            onCheckedChange = {
                                aceptaDatos = it
                                vm.clearBanners()
                            },
                            onPolicyClick = {
                                val uri = Uri.parse(PRIVACY_POLICY_URL)
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        )

                        if (habeasError) {
                            Text(
                                text = "Debes aceptar la política de tratamiento de datos.",
                                color = Color(0xFFD93025),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF5F8FC)
                        ) {
                            Text(
                                text = "La app ejecuta reCAPTCHA nativo automáticamente al registrarte.",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryText
                            )
                        }

                        PrimaryActionButton(
                            text = "Registrarse",
                            loading = state.loading,
                            enabled = idNumber.isNotBlank() &&
                                    name.isNotBlank() &&
                                    email.isNotBlank() &&
                                    password.isNotBlank() &&
                                    birthDateIso.isNotBlank() &&
                                    phone.isNotBlank() &&
                                    aceptaDatos,
                            onClick = {
                                vm.register(
                                    idNumber = idNumber.trim(),
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    phone = phone.trim(),
                                    birthDateIso = birthDateIso,
                                    role = Role.CLIENTE,
                                    aceptaHabeasData = aceptaDatos
                                )
                            }
                        )

                        if (!habeasError) {
                            nonSpecialError?.takeIf { it.isNotBlank() }?.let { errorText ->
                                ErrorBanner(errorText)
                            }
                        }

                        state.info?.takeIf { it.isNotBlank() }?.let { infoText ->
                            InfoBanner(infoText)
                        }

                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("¿Ya tienes cuenta? Inicia sesión")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }

            FloatingActionButton(
                onClick = { showAccessibilitySheet = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
                containerColor = SecondaryBlue,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text("♿", color = SurfaceWhite, style = MaterialTheme.typography.titleMedium)
            }

            FloatingActionButton(
                onClick = { showSupportSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp),
                containerColor = Color(0xFF2ECC71),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text("💬", color = SurfaceWhite, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PolicyRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onPolicyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Text(
            text = buildAnnotatedString {
                append("Acepto la ")
                pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                append("Política de Tratamiento de Datos")
                pop()
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { onPolicyClick() }
        )
    }
}

@Composable
private fun RegisterAccessibilitySheet(
    fontScale: Float,
    highContrast: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Accesibilidad",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryBlue,
            textAlign = TextAlign.Center
        )

        OutlinedButton(onClick = onIncrease, modifier = Modifier.fillMaxWidth()) {
            Text("Aumentar texto")
        }

        OutlinedButton(onClick = onDecrease, modifier = Modifier.fillMaxWidth()) {
            Text("Disminuir texto")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Contraste alto")
            Switch(
                checked = highContrast,
                onCheckedChange = { onToggleHighContrast() }
            )
        }

        Text(
            text = "Escala actual: ${"%.1f".format(fontScale)}x",
            color = MutedText
        )

        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text("Restablecer")
        }
    }
}

@Composable
private fun RegisterSupportSheet(
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    onEmail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Canales de comunicación",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryBlue,
            textAlign = TextAlign.Center
        )

        OutlinedButton(onClick = onWhatsApp, modifier = Modifier.fillMaxWidth()) {
            Text("WhatsApp")
        }

        OutlinedButton(onClick = onCall, modifier = Modifier.fillMaxWidth()) {
            Text("Llamar")
        }

        OutlinedButton(onClick = onEmail, modifier = Modifier.fillMaxWidth()) {
            Text("Correo")
        }
    }
}