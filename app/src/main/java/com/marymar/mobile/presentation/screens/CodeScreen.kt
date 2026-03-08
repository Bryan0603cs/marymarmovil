package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.AuthBackground
import com.marymar.mobile.ui.components.AuthCard
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.SecondaryActionButton

@Composable
fun CodeScreen(
    vm: AuthViewModel,
    email: String,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()
    var code by remember { mutableStateOf("") }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AuthCard(
                title = "Verifica tu acceso",
                subtitle = "Ingresa el código de seguridad que se envió a $email."
            ) {
                InfoBanner("Este flujo sigue el mismo proceso de la web: login, envío de código y validación final para generar la sesión.")
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código de verificación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (state.error != null) {
                    ErrorBanner(state.error ?: "")
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (state.info != null) {
                    InfoBanner(state.info ?: "")
                    Spacer(modifier = Modifier.height(14.dp))
                }
                PrimaryActionButton(
                    text = "Validar código",
                    loading = state.loading,
                    enabled = code.isNotBlank(),
                    onClick = { vm.validateCode(email, code.trim()) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryActionButton(
                    text = if (state.loadingResend) "Reenviando código..." else "Reenviar código",
                    onClick = { vm.resendCode(email) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Si el correo tarda en llegar, revisa spam o vuelve a enviar el código.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryActionButton(text = "Volver al login", onClick = onBack)
            }
        }
    }
}
