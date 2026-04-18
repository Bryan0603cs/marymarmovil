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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.R
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.AuthNext
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.RecaptchaWidget
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite
import java.util.Calendar

private val RegisterPrimary = Color(0xFF0F2B3D)
private val RegisterAccent = Color(0xFF2196F3)
private val RegisterFieldBg = Color(0xFFF2ECE6)
private val RegisterCaptchaBg = Color(0xFFECE7DF)
private val RegisterSuccess = Color(0xFF2E7D32)
private const val PRIVACY_POLICY_URL = "https://d3hmyhthxmr5gy.cloudfront.net/privacidad"
private const val REGISTER_SUPPORT_WHATSAPP = "573003710163"

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current

    var idNumber by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var birthDateDisplay by rememberSaveable { mutableStateOf("") }
    var birthDateIso by rememberSaveable { mutableStateOf("") }
    var aceptaDatos by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var captchaToken by rememberSaveable { mutableStateOf("") }
    var captchaError by rememberSaveable { mutableStateOf<String?>(null) }
    var reloadNonce by rememberSaveable { mutableIntStateOf(0) }
    var captchaHeightPx by rememberSaveable { mutableIntStateOf(180) }

    val scrollState = rememberScrollState()

    LaunchedEffect(state.next) {
        if (state.next == AuthNext.LoggedIn) {
            vm.consumeNext()
            onBack()
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val realMonth = month + 1
                birthDateDisplay = "%02d/%02d/%04d".format(dayOfMonth, realMonth, year)
                birthDateIso = "%04d-%02d-%02d".format(year, realMonth, dayOfMonth)
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
                    color = RegisterPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Crea tu cuenta para comenzar",
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
                        text = "Crear cuenta",
                        color = RegisterPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    RegisterFieldBox(
                        label = "IDENTIFICACIÓN",
                        value = idNumber,
                        onValueChange = {
                            idNumber = it
                            vm.clearBanners()
                        },
                        placeholder = "Número de identificación",
                        keyboardType = KeyboardType.Number
                    )

                    RegisterFieldBox(
                        label = "NOMBRE COMPLETO",
                        value = name,
                        onValueChange = {
                            name = it
                            vm.clearBanners()
                        },
                        placeholder = "Tu nombre completo",
                        keyboardType = KeyboardType.Text
                    )

                    RegisterFieldBox(
                        label = "CORREO ELECTRÓNICO",
                        value = email,
                        onValueChange = {
                            email = it
                            vm.clearBanners()
                        },
                        placeholder = "ejemplo@correo.com",
                        keyboardType = KeyboardType.Email
                    )

                    RegisterPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            vm.clearBanners()
                        },
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "FECHA DE NACIMIENTO",
                            color = RegisterPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 0.8.sp
                        )
                        OutlinedTextField(
                            value = birthDateDisplay,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            placeholder = {
                                Text("Selecciona tu fecha", color = MutedText.copy(alpha = 0.75f))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = RegisterFieldBg,
                                unfocusedContainerColor = RegisterFieldBg,
                                focusedBorderColor = RegisterAccent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = RegisterAccent
                            )
                        )
                    }

                    RegisterFieldBox(
                        label = "TELÉFONO",
                        value = phone,
                        onValueChange = {
                            phone = it
                            vm.clearBanners()
                        },
                        placeholder = "Número de teléfono",
                        keyboardType = KeyboardType.Phone
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Completa la verificación de seguridad",
                            color = RegisterPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = RegisterCaptchaBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                            captchaToken = it
                                            captchaError = null
                                        },
                                        onExpired = {
                                            captchaToken = ""
                                            captchaError = "La verificación expiró. Vuelve a completarla."
                                        },
                                        onError = {
                                            captchaToken = ""
                                            captchaError = it
                                        },
                                        onHeightChanged = { height ->
                                            captchaHeightPx = height.coerceIn(170, 280)
                                        }
                                    )
                                }

                                if (captchaToken.isNotBlank()) {
                                    Text(
                                        text = "Verificación completada",
                                        color = RegisterSuccess,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                captchaError?.let {
                                    Text(
                                        text = it,
                                        color = Color(0xFFD93025),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = aceptaDatos,
                            onCheckedChange = {
                                aceptaDatos = it
                                vm.clearBanners()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = RegisterAccent)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Acepto la Política de Tratamiento de Datos",
                                color = RegisterPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                                    )
                                }
                            )
                            Text(
                                text = "Debes aceptarla para crear la cuenta.",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    state.error?.takeIf { it.isNotBlank() }?.let { ErrorBanner(it) }
                    state.info?.takeIf { it.isNotBlank() }?.let { InfoBanner(it) }

                    Button(
                        onClick = {
                            if (captchaToken.isBlank()) {
                                captchaError = "Completa la verificación antes de registrarte."
                            } else {
                                vm.register(
                                    idNumber = idNumber.trim(),
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    phone = phone.trim(),
                                    birthDateIso = birthDateIso,
                                    role = Role.CLIENTE,
                                    aceptaHabeasData = aceptaDatos,
                                    captchaToken = captchaToken,
                                    captchaClient = "WEB"
                                )
                            }
                        },
                        enabled = idNumber.isNotBlank() &&
                                name.isNotBlank() &&
                                email.isNotBlank() &&
                                password.isNotBlank() &&
                                birthDateIso.isNotBlank() &&
                                phone.isNotBlank() &&
                                aceptaDatos &&
                                !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RegisterPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = RegisterPrimary.copy(alpha = 0.35f),
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
                            text = if (state.loading) "Registrando..." else "Registrarse",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("¿Ya tienes cuenta? ", color = MutedText)
                        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                            Text("Inicia sesión", color = RegisterPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        androidx.compose.material3.FloatingActionButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$REGISTER_SUPPORT_WHATSAPP"))
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
private fun RegisterFieldBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = RegisterPrimary,
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
                Text(placeholder, color = MutedText.copy(alpha = 0.75f))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RegisterFieldBg,
                unfocusedContainerColor = RegisterFieldBg,
                focusedBorderColor = RegisterAccent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = RegisterAccent
            )
        )
    }
}

@Composable
private fun RegisterPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "CONTRASEÑA",
            color = RegisterPrimary,
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
                        color = RegisterAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RegisterFieldBg,
                unfocusedContainerColor = RegisterFieldBg,
                focusedBorderColor = RegisterAccent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = RegisterAccent
            )
        )
    }
}