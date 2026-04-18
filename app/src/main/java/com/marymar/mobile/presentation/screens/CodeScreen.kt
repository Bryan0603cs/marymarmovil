package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.SoftSecondaryButton

private val CodeBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val CodePrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val CodeMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val CodeCard = androidx.compose.ui.graphics.Color.White

@Composable
fun CodeScreen(
    vm: AuthViewModel,
    email: String,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()
    var code by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CodeBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Código")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = CodeCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Escribe el código enviado",
                    color = CodePrimary,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = email,
                    color = CodeMuted,
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        vm.clearBanners()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    label = { Text("Código") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                state.error?.takeIf { it.isNotBlank() }?.let { ErrorBanner(it) }
                state.info?.takeIf { it.isNotBlank() }?.let { InfoBanner(it) }

                DarkPrimaryButton(
                    text = if (state.loading) "Validando..." else "Validar código",
                    enabled = code.isNotBlank() && !state.loading
                ) {
                    vm.validateCode(email, code.trim())
                }

                SoftSecondaryButton(
                    text = if (state.loadingResend) "Reenviando..." else "Reenviar código"
                ) {
                    vm.resendCode(email)
                }

                SoftSecondaryButton(text = "Volver") {
                    onBack()
                }
            }
        }
    }
}