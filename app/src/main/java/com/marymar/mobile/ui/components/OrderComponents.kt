package com.marymar.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val StatusCreated = Color(0xFF144A63)
private val StatusPreparation = Color(0xFF4B2783)
private val StatusReady = Color(0xFFD2C118)
private val StatusDelivered = Color(0xFF179A57)
private val StatusAccount = Color(0xFFE9722A)
private val StatusAvailable = Color(0xFF9AE2B1)
private val StatusDanger = Color(0xFFD64545)
private val StatusTextDark = Color(0xFF0D2530)

fun userFacingOrderStatus(status: String): String = when (status.uppercase()) {
    "CREADO" -> "Creado"
    "CONFIRMADO" -> "Creado"
    "EN_PREPARACION" -> "En preparación"
    "LISTO" -> "Listo"
    "ENTREGADO" -> "Entregado"
    "CUENTA_PEDIDA" -> "Cuenta pedida"
    "PAGADO" -> "Pagado"
    "CANCELADO" -> "Cancelado"
    else -> status.replace('_', ' ')
}

fun nextMeseroStatus(status: String): String? = when (status.uppercase()) {
    "LISTO" -> "ENTREGADO"
    "ENTREGADO" -> "CUENTA_PEDIDA"
    else -> null
}

fun nextMeseroActionLabel(status: String): String? = when (status.uppercase()) {
    "LISTO" -> "Marcar como entregado"
    "ENTREGADO" -> "Solicitar cuenta"
    else -> null
}

@Composable
fun OrderStatusBadge(status: String) {
    val (background, textColor, borderColor) = when (status.uppercase()) {
        "CREADO", "CONFIRMADO" -> Triple(StatusCreated.copy(alpha = 0.12f), StatusCreated, StatusCreated.copy(alpha = 0.35f))
        "EN_PREPARACION" -> Triple(StatusPreparation.copy(alpha = 0.12f), StatusPreparation, StatusPreparation.copy(alpha = 0.35f))
        "LISTO" -> Triple(StatusReady.copy(alpha = 0.18f), StatusTextDark, StatusReady.copy(alpha = 0.45f))
        "ENTREGADO", "PAGADO" -> Triple(StatusDelivered.copy(alpha = 0.12f), StatusDelivered, StatusDelivered.copy(alpha = 0.35f))
        "CUENTA_PEDIDA" -> Triple(StatusAccount.copy(alpha = 0.14f), StatusAccount, StatusAccount.copy(alpha = 0.35f))
        "CANCELADO" -> Triple(StatusDanger.copy(alpha = 0.12f), StatusDanger, StatusDanger.copy(alpha = 0.35f))
        else -> Triple(StatusAvailable.copy(alpha = 0.16f), StatusTextDark, StatusAvailable.copy(alpha = 0.45f))
    }

    Box(
        modifier = Modifier
            .background(background, androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .border(1.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = userFacingOrderStatus(status),
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}