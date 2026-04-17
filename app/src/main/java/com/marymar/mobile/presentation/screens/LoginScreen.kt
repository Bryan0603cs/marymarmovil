package com.marymar.mobile.presentation.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.R
import com.marymar.mobile.core.auth.GoogleSignInManager
import com.marymar.mobile.presentation.viewmodel.AuthNext
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SecondaryBlue
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

private const val SUPPORT_PHONE = "573003710163"
private const val SUPPORT_EMAIL = "soporte@marymar.com"
private const val SUPPORT_WHATSAPP = "573003710163"

private val NavyDark = Color(0xFF0F2B3D)
private val FieldBg  = Color(0xFFF5EFE8)
private val CaptchaBg = Color(0xFFEEEAE3)
private val BlueLine  = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    googleSignInManager: GoogleSignInManager,
    onRegister: () -> Unit,
    onGoToCode: (String) -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val currentDensity = LocalDensity.current
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showAccessibilitySheet by rememberSaveable { mutableStateOf(false) }
    var showSupportSheet by rememberSaveable { mutableStateOf(false) }
    var fontScale by rememberSaveable { mutableStateOf(1f) }
    var highContrast by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val supportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accessibilitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgColor      = if (highContrast) Color(0xFF14181C) else SoftBeige
    val cardColor    = if (highContrast) Color(0xFFF6F6F6) else SurfaceWhite
    val primaryText  = if (highContrast) Color(0xFF0E3445) else NavyDark
    val secondaryText = if (highContrast) Color(0xFF313131) else MutedText

    LaunchedEffect(state.next) {
        when (val next = state.next) {
            is AuthNext.GoToCode -> { vm.consumeNext(); onGoToCode(next.email) }
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
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$SUPPORT_WHATSAPP")))
                    },
                    onCall = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_PHONE")))
                    },
                    onEmail = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$SUPPORT_EMAIL")
                            putExtra(Intent.EXTRA_SUBJECT, "Soporte Mar y Mar")
                        })
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
                    onReset = { fontScale = 1f; highContrast = false }
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
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /* ── HEADER ── */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mar_y_mar),
                        contentDescription = "Logo Mar y Mar",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MAR Y MAR",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryText,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(30.dp))

                /* ── CARD ── */
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        /* Título Welcome + línea azul */
                        Column {
                            Text(
                                text = "Welcome",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BlueLine)
                            )
                        }

                        /* Campo email */
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CORREO ELECTRÓNICO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText,
                                letterSpacing = 0.8.sp
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; vm.clearBanners() },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(50.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                placeholder = { Text("ejemplo@correo.com", color = MutedText.copy(alpha = 0.7f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = FieldBg,
                                    focusedContainerColor = FieldBg,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = BlueLine
                                )
                            )
                        }

                        /* Campo contraseña */
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CONTRASEÑA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText,
                                letterSpacing = 0.8.sp
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; vm.clearBanners() },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(50.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                placeholder = { Text("••••••••", color = MutedText) },
                                trailingIcon = {
                                    TextButton(onClick = { showPassword = !showPassword }) {
                                        Text(
                                            text = if (showPassword) "OCULTAR" else "MOSTRAR",
                                            color = BlueLine,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = FieldBg,
                                    focusedContainerColor = FieldBg,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = BlueLine
                                )
                            )
                        }

                        /* reCAPTCHA visual (el nativo corre en background al presionar Ingresar) */
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            color = CaptchaBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Text(
                                        text = "No soy un robot",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = primaryText
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF4A5568)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.White, fontSize = 14.sp)
                                    }
                                    Text(text = "reCAPTCHA", style = MaterialTheme.typography.labelSmall, color = secondaryText, fontSize = 8.sp)
                                }
                            }
                        }

                        /* Errores / Info */
                        state.error?.takeIf { it.isNotBlank() }?.let { ErrorBanner(it) }
                        state.info?.takeIf { it.isNotBlank() }?.let { InfoBanner(it) }

                        /* Botón Ingresar */
                        Button(
                            onClick = { vm.login(email.trim(), password) },
                            enabled = email.isNotBlank() && password.isNotBlank() && !state.loading && !state.loadingGoogle,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyDark,
                                contentColor = SurfaceWhite,
                                disabledContainerColor = NavyDark.copy(alpha = 0.45f),
                                disabledContentColor = SurfaceWhite.copy(alpha = 0.55f)
                            )
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SurfaceWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (state.loading) "Ingresando..." else "Ingresar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        /* Botón Google */
                        OutlinedButton(
                            onClick = {
                                val activity = context as? Activity
                                if (activity == null) {
                                    vm.cancelGoogleLogin("No fue posible abrir el selector de Google en este contexto")
                                } else {
                                    vm.startGoogleLoginFlow()
                                    scope.launch {
                                        googleSignInManager.requestGoogleIdToken(activity)
                                            .onSuccess { vm.loginWithGoogle(it) }
                                            .onFailure { vm.cancelGoogleLogin(googleSignInManager.toUserMessage(it)) }
                                    }
                                }
                            },
                            enabled = !state.loading && !state.loadingGoogle,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceWhite,
                                contentColor = Color(0xFF1F2937)
                            )
                        ) {
                            if (state.loadingGoogle) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Conectando con Google...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            } else {
                                Box(
                                    modifier = Modifier.size(22.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Continuar con Google", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        /* ¿Olvidaste tu contraseña? */
                        TextButton(
                            onClick = { vm.forgotPassword(email.trim()) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (state.loadingForgot) "Enviando..." else "¿Olvidaste tu contraseña?",
                                color = BlueLine,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        /* ¿No tienes cuenta? Regístrate */
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("¿No tienes cuenta? ", color = secondaryText, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                                Text("Regístrate", color = primaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp))
            }

            /* FABs */
            FloatingActionButton(
                onClick = { showAccessibilitySheet = true },
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
                containerColor = SecondaryBlue, shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) { Text("♿", color = SurfaceWhite, style = MaterialTheme.typography.titleMedium) }

            FloatingActionButton(
                onClick = { showSupportSheet = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp),
                containerColor = Color(0xFF2ECC71), shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) { Text("💬", color = SurfaceWhite, style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun LoginAccessibilitySheet(
    fontScale: Float, highContrast: Boolean,
    onIncrease: () -> Unit, onDecrease: () -> Unit,
    onToggleHighContrast: () -> Unit, onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Accesibilidad", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, color = PrimaryBlue, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onIncrease, modifier = Modifier.fillMaxWidth()) { Text("Aumentar texto") }
        OutlinedButton(onClick = onDecrease, modifier = Modifier.fillMaxWidth()) { Text("Disminuir texto") }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Contraste alto")
            Switch(checked = highContrast, onCheckedChange = { onToggleHighContrast() })
        }
        Text("Escala actual: ${"%.1f".format(fontScale)}x", color = MutedText)
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Restablecer") }
    }
}

@Composable
private fun LoginSupportSheet(onWhatsApp: () -> Unit, onCall: () -> Unit, onEmail: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Canales de comunicación", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, color = PrimaryBlue, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onWhatsApp, modifier = Modifier.fillMaxWidth()) { Text("WhatsApp") }
        OutlinedButton(onClick = onCall, modifier = Modifier.fillMaxWidth()) { Text("Llamar") }
        OutlinedButton(onClick = onEmail, modifier = Modifier.fillMaxWidth()) { Text("Correo") }
    }
}
