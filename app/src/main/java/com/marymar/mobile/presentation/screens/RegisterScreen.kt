package com.marymar.mobile.presentation.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.marymar.mobile.R
import com.marymar.mobile.core.auth.GoogleSignInManager
import com.marymar.mobile.domain.model.Role
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
import java.util.Calendar

private const val REGISTER_SUPPORT_PHONE = "573003710163"
private const val REGISTER_SUPPORT_EMAIL = "soporte@marymar.com"
private const val REGISTER_SUPPORT_WHATSAPP = "573003710163"
private const val PRIVACY_POLICY_URL = "https://d3hmyhthxmr5gy.cloudfront.net/politica"

private val RegNavyDark  = Color(0xFF0F2B3D)
private val RegFieldBg   = Color(0xFFF5EFE8)
private val RegBlueLine  = Color(0xFF2196F3)

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

    val bgColor       = if (highContrast) Color(0xFF14181C) else SoftBeige
    val cardColor     = if (highContrast) Color(0xFFF6F6F6) else SurfaceWhite
    val primaryText   = if (highContrast) Color(0xFF0E3445) else RegNavyDark
    val secondaryText = if (highContrast) Color(0xFF313131) else MutedText

    val habeasError = state.error?.contains("habeas", ignoreCase = true) == true ||
            state.error?.contains("tratamiento de datos", ignoreCase = true) == true
    val nonSpecialError = state.error?.takeIf {
        !it.contains("habeas", ignoreCase = true) && !it.contains("tratamiento de datos", ignoreCase = true)
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val m = month + 1
            birthDateDisplay = "%02d/%02d/%04d".format(dayOfMonth, m, year)
            birthDateIso = "%04d-%02d-%02d".format(year, m, dayOfMonth)
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
                sheetState = supportSheetState, containerColor = SurfaceWhite
            ) {
                RegisterSupportSheet(
                    onWhatsApp = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$REGISTER_SUPPORT_WHATSAPP"))) },
                    onCall     = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$REGISTER_SUPPORT_PHONE"))) },
                    onEmail    = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$REGISTER_SUPPORT_EMAIL")
                            putExtra(Intent.EXTRA_SUBJECT, "Soporte Mar y Mar")
                        })
                    }
                )
            }
        }

        if (showAccessibilitySheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccessibilitySheet = false },
                sheetState = accessibilitySheetState, containerColor = SurfaceWhite
            ) {
                RegisterAccessibilitySheet(
                    fontScale = fontScale, highContrast = highContrast,
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
                    text = "Crea tu cuenta para comenzar",
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
                        /* Título + línea azul */
                        Column {
                            Text(
                                text = "Crear cuenta",
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
                                    .background(RegBlueLine)
                            )
                        }

                        /* Helper: campo genérico con etiqueta */
                        @Composable
                        fun LabeledField(
                            label: String,
                            value: String,
                            onValueChange: (String) -> Unit,
                            placeholder: String,
                            keyboardType: KeyboardType = KeyboardType.Text,
                            readOnly: Boolean = false,
                            trailingIcon: @Composable (() -> Unit)? = null,
                            visualTransformation: VisualTransformation = VisualTransformation.None
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText,
                                    letterSpacing = 0.8.sp
                                )
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    readOnly = readOnly,
                                    shape = RoundedCornerShape(50.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                                    visualTransformation = visualTransformation,
                                    placeholder = { Text(placeholder, color = MutedText.copy(alpha = 0.7f)) },
                                    trailingIcon = trailingIcon,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = RegFieldBg,
                                        focusedContainerColor = RegFieldBg,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = RegBlueLine
                                    )
                                )
                            }
                        }

                        LabeledField(
                            label = "IDENTIFICACIÓN",
                            value = idNumber,
                            onValueChange = { idNumber = it; vm.clearBanners() },
                            placeholder = "Número de identificación",
                            keyboardType = KeyboardType.Number
                        )

                        LabeledField(
                            label = "NOMBRE COMPLETO",
                            value = name,
                            onValueChange = { name = it; vm.clearBanners() },
                            placeholder = "Tu nombre completo"
                        )

                        LabeledField(
                            label = "CORREO ELECTRÓNICO",
                            value = email,
                            onValueChange = { email = it; vm.clearBanners() },
                            placeholder = "ejemplo@correo.com",
                            keyboardType = KeyboardType.Email
                        )

                        /* Contraseña con mostrar/ocultar */
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
                                            color = RegBlueLine,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = RegFieldBg,
                                    focusedContainerColor = RegFieldBg,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = RegBlueLine
                                )
                            )
                        }

                        /* Fecha de nacimiento */
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "FECHA DE NACIMIENTO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText,
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
                                placeholder = { Text("DD/MM/AAAA", color = MutedText.copy(alpha = 0.7f)) },
                                trailingIcon = {
                                    TextButton(onClick = { datePickerDialog.show() }) { Text("📅") }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = RegFieldBg,
                                    focusedContainerColor = RegFieldBg,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = RegBlueLine
                                )
                            )
                        }

                        LabeledField(
                            label = "TELÉFONO",
                            value = phone,
                            onValueChange = { phone = it; vm.clearBanners() },
                            placeholder = "Número de teléfono",
                            keyboardType = KeyboardType.Phone
                        )

                        /* Habeas data */
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = aceptaDatos,
                                onCheckedChange = { aceptaDatos = it; vm.clearBanners() },
                                colors = CheckboxDefaults.colors(checkedColor = RegBlueLine)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Acepto la ")
                                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = RegBlueLine))
                                    append("Política de Tratamiento de Datos")
                                    pop()
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                                }
                            )
                        }

                        if (habeasError) {
                            Text(
                                text = "Debes aceptar la política de tratamiento de datos.",
                                color = Color(0xFFD93025),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        /* Errores / Info */
                        if (!habeasError) nonSpecialError?.takeIf { it.isNotBlank() }?.let { ErrorBanner(it) }
                        state.info?.takeIf { it.isNotBlank() }?.let { InfoBanner(it) }

                        /* Botón Registrarse */
                        Button(
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
                            },
                            enabled = idNumber.isNotBlank() && name.isNotBlank() &&
                                    email.isNotBlank() && password.isNotBlank() &&
                                    birthDateIso.isNotBlank() && phone.isNotBlank() &&
                                    aceptaDatos && !state.loading,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RegNavyDark,
                                contentColor = SurfaceWhite,
                                disabledContainerColor = RegNavyDark.copy(alpha = 0.45f),
                                disabledContentColor = SurfaceWhite.copy(alpha = 0.55f)
                            )
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SurfaceWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (state.loading) "Registrando..." else "Registrarse",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        /* Ya tengo cuenta → Inicia sesión */
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("¿Ya tienes cuenta? ", color = secondaryText, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                                Text("Inicia sesión", color = primaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
private fun RegisterAccessibilitySheet(
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
private fun RegisterSupportSheet(onWhatsApp: () -> Unit, onCall: () -> Unit, onEmail: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Canales de comunicación", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, color = PrimaryBlue, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onWhatsApp, modifier = Modifier.fillMaxWidth()) { Text("WhatsApp") }
        OutlinedButton(onClick = onCall, modifier = Modifier.fillMaxWidth()) { Text("Llamar") }
        OutlinedButton(onClick = onEmail, modifier = Modifier.fillMaxWidth()) { Text("Correo") }
    }
}
