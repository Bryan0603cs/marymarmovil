package com.marymar.mobile.presentation.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.marymar.mobile.ui.components.RecaptchaWidget
import com.marymar.mobile.ui.components.SecondaryActionButton
import com.marymar.mobile.ui.theme.BorderGray
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SecondaryBlue
import com.marymar.mobile.ui.theme.SurfaceWhite
import java.util.Calendar

private const val SUPPORT_PHONE = "573003710163"
private const val SUPPORT_EMAIL = "soporte@marymar.com"
private const val SUPPORT_WHATSAPP = "573003710163"
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

    var captchaReloadNonce by rememberSaveable { mutableStateOf(0) }
    var previousCaptchaVerified by rememberSaveable { mutableStateOf(false) }
    var captchaLocalError by rememberSaveable { mutableStateOf<String?>(null) }
    var captchaHeight by rememberSaveable { mutableStateOf(96) }

    val scrollState = rememberScrollState()
    val supportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accessibilitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = if (highContrast) Color(0xFF0A2E3B) else PrimaryBlue
    val cardColor = if (highContrast) Color(0xFFF7F7F7) else Color(0xFFF3F3F3)
    val primaryText = if (highContrast) Color(0xFF102129) else PrimaryBlue
    val secondaryText = if (highContrast) Color(0xFF222222) else MutedText

    val captchaError = state.error?.contains("captcha", ignoreCase = true) == true
    val habeasError = state.error?.contains("habeas", ignoreCase = true) == true ||
            state.error?.contains("tratamiento de datos", ignoreCase = true) == true

    val nonSpecialError = state.error?.takeIf {
        !it.contains("captcha", ignoreCase = true) &&
                !it.contains("habeas", ignoreCase = true) &&
                !it.contains("tratamiento de datos", ignoreCase = true)
    }

    LaunchedEffect(state.captchaVerified) {
        if (previousCaptchaVerified && !state.captchaVerified) {
            captchaReloadNonce += 1
        }
        previousCaptchaVerified = state.captchaVerified
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
        LocalDensity provides Density(currentDensity.density, currentDensity.fontScale * fontScale)
    ) {
        if (showSupportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSupportSheet = false },
                sheetState = supportSheetState,
                containerColor = SurfaceWhite
            ) {
                RegisterSupportChannelSheet(
                    onWhatsApp = {
                        val uri = Uri.parse("https://wa.me/$SUPPORT_WHATSAPP")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    onCall = {
                        val uri = Uri.parse("tel:$SUPPORT_PHONE")
                        context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                    },
                    onEmail = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$SUPPORT_EMAIL")
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
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mar_y_mar),
                            contentDescription = "Logo Mar y Mar",
                            modifier = Modifier
                                .size(84.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = "Crear cuenta",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryText,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Regístrate para disfrutar de nuestros platos",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = {
                                idNumber = it
                                captchaLocalError = null
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("Identificación", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                captchaLocalError = null
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("Nombre completo", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                captchaLocalError = null
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("Correo electrónico", color = MutedText) }
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                captchaLocalError = null
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                            shape = RoundedCornerShape(14.dp),
                            placeholder = { Text("dd/mm/aaaa", color = MutedText) },
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
                                captchaLocalError = null
                                vm.clearBanners()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (state.captchaVerified) Color(0xFF2E7D32) else BorderGray,
                                    RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceWhite
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                if (captchaError || captchaLocalError != null) {
                                    Text(
                                        text = captchaLocalError
                                            ?: "La verificación ha caducado. Vuelve a marcar la casilla de verificación.",
                                        color = Color(0xFFD93025),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                RecaptchaWidget(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(captchaHeight.dp),
                                    reloadNonce = captchaReloadNonce,
                                    onTokenReceived = { token ->
                                        captchaLocalError = null
                                        captchaHeight = 96
                                        vm.setCaptchaToken(token)
                                    },
                                    onExpired = {
                                        captchaLocalError = "La verificación ha caducado. Vuelve a marcar la casilla."
                                        captchaHeight = 96
                                        vm.clearCaptcha()
                                    },
                                    onError = { message ->
                                        captchaLocalError = message
                                        captchaHeight = 96
                                        vm.clearCaptcha()
                                    },
                                    onHeightChanged = { newHeight ->
                                        captchaHeight = when {
                                            newHeight > 180 -> newHeight.coerceIn(520, 700)
                                            else -> newHeight.coerceIn(96, 120)
                                        }
                                    }
                                )
                            }
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
                                    aceptaDatos &&
                                    state.captchaVerified,
                            onClick = {
                                if (!state.captchaVerified) {
                                    captchaLocalError = "Completa el captcha"
                                } else {
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
                            }
                        )

                        if (!captchaError && !habeasError) {
                            nonSpecialError?.let { errorText ->
                                if (errorText.isNotBlank()) {
                                    ErrorBanner(errorText)
                                }
                            }
                        }

                        state.info?.let { infoText ->
                            if (infoText.isNotBlank()) {
                                InfoBanner(infoText)
                            }
                        }

                        SecondaryActionButton(
                            text = "¿Ya tienes cuenta? Inicia sesión",
                            onClick = onBack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(88.dp))
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
            style = MaterialTheme.typography.bodyLarge,
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

        SecondaryActionButton(
            text = "Restablecer",
            onClick = onReset
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RegisterSupportChannelSheet(
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

        Spacer(modifier = Modifier.height(8.dp))
    }
}