package com.marymar.mobile.presentation.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.R
import com.marymar.mobile.core.auth.GoogleSignInManager
import com.marymar.mobile.presentation.viewmodel.AuthNext
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.RecaptchaWidget
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

private val LoginPrimary = Color(0xFF0F2B3D)
private val LoginAccent = Color(0xFF2196F3)
private val LoginFieldBg = Color(0xFFF2ECE6)
private val LoginCaptchaBg = Color(0xFFECE7DF)
private val LoginSuccess = Color(0xFF2E7D32)
private const val LOGIN_SUPPORT_WHATSAPP = "573003710163"

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    googleSignInManager: GoogleSignInManager,
    onRegister: () -> Unit,
    onGoToCode: (String) -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var manualCaptchaRequired by rememberSaveable { mutableStateOf(false) }
    var manualCaptchaToken by rememberSaveable { mutableStateOf("") }
    var manualCaptchaError by rememberSaveable { mutableStateOf<String?>(null) }
    var lastAttemptWasAutomatic by rememberSaveable { mutableStateOf(false) }
    var reloadNonce by rememberSaveable { mutableIntStateOf(0) }
    var captchaHeightPx by rememberSaveable { mutableIntStateOf(180) }

    val scrollState = rememberScrollState()

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

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrBlank() && lastAttemptWasAutomatic) {
            manualCaptchaRequired = true
            manualCaptchaToken = ""
            manualCaptchaError = "Completa la verificación manual para continuar."
            reloadNonce += 1
            lastAttemptWasAutomatic = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_mar_y_mar),
                    contentDescription = "Logo Mar y Mar",
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MAR Y MAR",
                    color = LoginPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Inicia sesión para continuar",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = LoginPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    LoginField(
                        label = "CORREO ELECTRÓNICO",
                        value = email,
                        onValueChange = {
                            email = it
                            vm.clearBanners()
                            manualCaptchaError = null
                        },
                        placeholder = "ejemplo@correo.com",
                        keyboardType = KeyboardType.Email
                    )

                    LoginPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            vm.clearBanners()
                            manualCaptchaError = null
                        },
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    if (manualCaptchaRequired) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = LoginCaptchaBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Completa la verificación de seguridad",
                                    color = LoginPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(with(density) { captchaHeightPx.toDp() }),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    RecaptchaWidget(
                                        modifier = Modifier.fillMaxSize(),
                                        reloadNonce = reloadNonce,
                                        onTokenReceived = {
                                            manualCaptchaToken = it
                                            manualCaptchaError = null
                                        },
                                        onExpired = {
                                            manualCaptchaToken = ""
                                            manualCaptchaError = "La verificación expiró. Vuelve a completarla."
                                        },
                                        onError = {
                                            manualCaptchaToken = ""
                                            manualCaptchaError = it
                                        },
                                        onHeightChanged = { height ->
                                            captchaHeightPx = height.coerceIn(170, 280)
                                        }
                                    )
                                }

                                if (manualCaptchaToken.isNotBlank()) {
                                    Text(
                                        text = "Verificación completada",
                                        color = LoginSuccess,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                manualCaptchaError?.let {
                                    Text(
                                        text = it,
                                        color = Color(0xFFD93025),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = LoginCaptchaBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Verificación de seguridad automática",
                                    color = LoginPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFD5D1C9),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(
                                                color = Color(0xFFEDF7ED),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF2E7D32),
                                                shape = RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "No soy un robot",
                                        color = LoginPrimary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Image(
                                        painter = painterResource(id = R.drawable.ic_recaptcha_logo),
                                        contentDescription = "reCAPTCHA",
                                        modifier = Modifier.size(28.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    state.error?.takeIf { it.isNotBlank() }?.let { ErrorBanner(it) }
                    state.info?.takeIf { it.isNotBlank() }?.let { InfoBanner(it) }

                    Button(
                        onClick = {
                            vm.clearBanners()
                            if (manualCaptchaRequired) {
                                if (manualCaptchaToken.isBlank()) {
                                    manualCaptchaError = "Completa la verificación antes de continuar."
                                } else {
                                    lastAttemptWasAutomatic = false
                                    vm.login(
                                        email = email.trim(),
                                        password = password,
                                        captchaToken = manualCaptchaToken,
                                        captchaClient = "WEB"
                                    )
                                }
                            } else {
                                lastAttemptWasAutomatic = true
                                vm.login(email.trim(), password)
                            }
                        },
                        enabled = email.isNotBlank() &&
                                password.isNotBlank() &&
                                !state.loading &&
                                !state.loadingGoogle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoginPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = LoginPrimary.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.55f)
                        )
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (state.loading) "Ingresando..." else "Ingresar",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val activity = context as? Activity
                            if (activity == null) {
                                vm.cancelGoogleLogin("No fue posible abrir el selector de Google en este contexto")
                            } else {
                                vm.startGoogleLoginFlow()
                                scope.launch {
                                    googleSignInManager.requestGoogleIdToken(activity)
                                        .onSuccess { idToken ->
                                            vm.loginWithGoogle(idToken)
                                        }
                                        .onFailure {
                                            vm.cancelGoogleLogin(googleSignInManager.toUserMessage(it))
                                        }
                                }
                            }
                        },
                        enabled = !state.loading && !state.loadingGoogle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceWhite,
                            contentColor = LoginPrimary
                        )
                    ) {
                        if (state.loadingGoogle) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = LoginAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Conectando con Google...")
                        } else {
                            Text("G", color = LoginAccent, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continuar con Google", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    TextButton(
                        onClick = { vm.forgotPassword(email.trim()) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (state.loadingForgot) "Enviando..." else "¿Olvidaste tu contraseña?",
                            color = LoginAccent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("¿No tienes cuenta? ", color = MutedText)
                        TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                            Text("Regístrate", color = LoginPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        androidx.compose.material3.FloatingActionButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$LOGIN_SUPPORT_WHATSAPP"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp),
            containerColor = Color(0xFF25D366),
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "💬",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun LoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = LoginPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = {
                Text(text = placeholder, color = MutedText.copy(alpha = 0.75f))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LoginFieldBg,
                unfocusedContainerColor = LoginFieldBg,
                focusedBorderColor = LoginAccent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = LoginAccent
            )
        )
    }
}

@Composable
private fun LoginPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "CONTRASEÑA",
            color = LoginPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.8.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(50.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            placeholder = {
                Text("••••••••", color = MutedText.copy(alpha = 0.75f))
            },
            trailingIcon = {
                TextButton(onClick = onTogglePassword) {
                    Text(
                        text = if (showPassword) "OCULTAR" else "MOSTRAR",
                        color = LoginAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LoginFieldBg,
                unfocusedContainerColor = LoginFieldBg,
                focusedBorderColor = LoginAccent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = LoginAccent
            )
        )
    }
}