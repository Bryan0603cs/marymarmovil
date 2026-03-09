package com.marymar.mobile.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.marymar.mobile.R
import com.marymar.mobile.presentation.viewmodel.AuthNext
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.RecaptchaWidget
import com.marymar.mobile.ui.theme.BorderGray
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SecondaryBlue
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite

private const val SUPPORT_PHONE = "573003710163"
private const val SUPPORT_EMAIL = "soporte@marymar.com"
private const val SUPPORT_WHATSAPP = "573003710163"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onRegister: () -> Unit,
    onGoToCode: (String) -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val currentDensity = LocalDensity.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var showAccessibilitySheet by rememberSaveable { mutableStateOf(false) }
    var showSupportSheet by rememberSaveable { mutableStateOf(false) }

    var fontScale by rememberSaveable { mutableStateOf(1f) }
    var highContrast by rememberSaveable { mutableStateOf(false) }

    var captchaReloadNonce by rememberSaveable { mutableStateOf(0) }
    var previousCaptchaVerified by rememberSaveable { mutableStateOf(false) }
    var captchaLocalError by rememberSaveable { mutableStateOf<String?>(null) }
    var captchaHeight by rememberSaveable { mutableStateOf(88) }

    val animatedCaptchaHeight by animateDpAsState(
        targetValue = captchaHeight.dp,
        label = "loginCaptchaHeight"
    )

    val scrollState = rememberScrollState()
    val supportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accessibilitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor = if (highContrast) Color(0xFF14181C) else SoftBeige
    val cardColor = if (highContrast) Color(0xFFF6F6F6) else SurfaceWhite
    val headerColor = if (highContrast) Color(0xFF1D242B) else SurfaceWhite
    val primaryText = if (highContrast) Color(0xFF0E3445) else PrimaryBlue
    val secondaryText = if (highContrast) Color(0xFF313131) else MutedText

    val captchaError = state.error?.contains("captcha", ignoreCase = true) == true
    val nonCaptchaError = state.error?.takeIf { !it.contains("captcha", ignoreCase = true) }

    LaunchedEffect(state.captchaVerified) {
        if (previousCaptchaVerified && !state.captchaVerified) {
            captchaReloadNonce += 1
        }
        previousCaptchaVerified = state.captchaVerified
    }

    LaunchedEffect(state.next) {
        when (val next = state.next) {
            is AuthNext.GoToCode -> {
                vm.consumeNext()
                onGoToCode(next.email)
            }

            AuthNext.LoggedIn -> vm.consumeNext()
            null -> Unit
        }
    }

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
                LoginSupportSheet(
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
                LoginAccessibilitySheet(
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
                                text = "Inicia sesión para continuar",
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
                            text = "Bienvenido a Mar y Mar",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = primaryText,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ingresa con tu correo y contraseña",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                            textAlign = TextAlign.Center
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
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (state.captchaVerified) Color(0xFF2E7D32) else BorderGray,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceWhite
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                if (captchaError || captchaLocalError != null) {
                                    Text(
                                        text = captchaLocalError
                                            ?: "La verificación ha caducado. Vuelve a marcar la casilla.",
                                        color = Color(0xFFD93025),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                RecaptchaWidget(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(animatedCaptchaHeight),
                                    reloadNonce = captchaReloadNonce,
                                    onTokenReceived = { token ->
                                        captchaLocalError = null
                                        captchaHeight = 88
                                        vm.setCaptchaToken(token)
                                    },
                                    onExpired = {
                                        captchaLocalError =
                                            "La verificación ha caducado. Vuelve a marcar la casilla."
                                        captchaHeight = 88
                                        vm.clearCaptcha()
                                    },
                                    onError = { message ->
                                        captchaLocalError = message
                                        captchaHeight = 88
                                        vm.clearCaptcha()
                                    },
                                    onHeightChanged = { newHeight ->
                                        captchaHeight = newHeight.coerceIn(88, 540)
                                    }
                                )
                            }
                        }

                        PrimaryActionButton(
                            text = "Ingresar",
                            loading = state.loading,
                            enabled = email.isNotBlank() &&
                                    password.isNotBlank() &&
                                    state.captchaVerified,
                            onClick = {
                                if (!state.captchaVerified) {
                                    captchaLocalError = "Completa el captcha"
                                } else {
                                    vm.login(email.trim(), password)
                                }
                            }
                        )

                        if (!captchaError) {
                            nonCaptchaError?.takeIf { it.isNotBlank() }?.let { errorText ->
                                ErrorBanner(errorText)
                            }
                        }

                        state.info?.takeIf { it.isNotBlank() }?.let { infoText ->
                            InfoBanner(infoText)
                        }

                        LoginGoogleButton(
                            onClick = { vm.onGoogleLoginRequested() }
                        )

                        TextButton(
                            onClick = { vm.forgotPassword(email.trim()) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (state.loadingForgot) {
                                    "Enviando..."
                                } else {
                                    "¿Olvidaste tu contraseña?"
                                },
                                color = primaryText
                            )
                        }

                        RegisterCtaButton(onClick = onRegister)
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
private fun LoginGoogleButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceWhite,
            contentColor = Color(0xFF1F2937)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "G",
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "Continuar con Google",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun RegisterCtaButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryBlue,
            contentColor = SurfaceWhite
        )
    ) {
        Text(
            text = "¿No tienes cuenta? Regístrate",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoginAccessibilitySheet(
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
private fun LoginSupportSheet(
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