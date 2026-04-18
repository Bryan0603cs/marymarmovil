package com.marymar.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.BorderGray
import com.marymar.mobile.ui.theme.ErrorRed
import com.marymar.mobile.ui.theme.PrimaryBlue
import com.marymar.mobile.ui.theme.SuccessGreen
import com.marymar.mobile.ui.theme.SurfaceWhite

fun userFacingOrderStatus(status: String): String = when (status.uppercase()) {
    "CREADO" -> "Pedido recibido"
    "CONFIRMADO" -> "Pedido confirmado"
    "EN_PREPARACION" -> "En preparación"
    "LISTO" -> "Terminado"
    "ENTREGADO" -> "Entregado"
    "CUENTA_PEDIDA" -> "Cuenta solicitada"
    "PAGADO" -> "Pagado"
    "CANCELADO" -> "Cancelado"
    else -> status.replace('_', ' ')
}

fun nextMeseroStatus(status: String): String? = when (status.uppercase()) {
    "CREADO", "CONFIRMADO" -> "EN_PREPARACION"
    "LISTO" -> "ENTREGADO"
    "ENTREGADO" -> "CUENTA_PEDIDA"
    else -> null
}

fun nextMeseroActionLabel(status: String): String? = when (status.uppercase()) {
    "CREADO", "CONFIRMADO" -> "Enviar a cocina"
    "LISTO" -> "Marcar como entregado"
    "ENTREGADO" -> "Solicitar cuenta"
    else -> null
}

fun orderSteps(type: String): List<String> = when (type.uppercase()) {
    "MESA" -> listOf("CREADO", "EN_PREPARACION", "LISTO", "ENTREGADO", "CUENTA_PEDIDA")
    else -> listOf("CREADO", "EN_PREPARACION", "LISTO", "ENTREGADO")
}

private fun completedStepIndex(status: String, type: String): Int {
    val upperStatus = status.uppercase()
    if (upperStatus == "CANCELADO") return -1
    val steps = orderSteps(type)
    return steps.indexOf(upperStatus).coerceAtLeast(0)
}

@Composable
fun OrderStatusBadge(status: String) {
    val background = when (status.uppercase()) {
        "ENTREGADO", "PAGADO" -> SuccessGreen.copy(alpha = 0.12f)
        "CANCELADO" -> ErrorRed.copy(alpha = 0.12f)
        "LISTO", "CUENTA_PEDIDA" -> AccentOrange.copy(alpha = 0.12f)
        else -> PrimaryBlue.copy(alpha = 0.1f)
    }
    val textColor = when (status.uppercase()) {
        "ENTREGADO", "PAGADO" -> SuccessGreen
        "CANCELADO" -> ErrorRed
        "LISTO", "CUENTA_PEDIDA" -> AccentOrange
        else -> PrimaryBlue
    }

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
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

@Composable
fun OrderProgressTimeline(status: String, type: String) {
    val steps = orderSteps(type)
    val completedIndex = completedStepIndex(status, type)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        steps.forEachIndexed { index, step ->
            val completed = completedIndex >= index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (completed) AccentOrange else SurfaceWhite,
                            CircleShape
                        )
                        .border(1.dp, if (completed) AccentOrange else BorderGray, CircleShape)
                )
                Text(
                    text = userFacingOrderStatus(step),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (index != steps.lastIndex) {
                Spacer(modifier = Modifier.size(2.dp))
            }
        }
    }
}
