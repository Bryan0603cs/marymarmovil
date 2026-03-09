package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
                title = "Código de verificación",
                subtitle = ""
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        vm.clearBanners()
                    },
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                state.error?.takeIf { it.isNotBlank() }?.let {
                    ErrorBanner(it)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                state.info?.takeIf { it.isNotBlank() }?.let {
                    InfoBanner(it)
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
                    text = if (state.loadingResend) "Reenviando..." else "Reenviar código",
                    onClick = { vm.resendCode(email) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SecondaryActionButton(
                    text = "Volver",
                    onClick = onBack
                )
            }
        }
    }
}