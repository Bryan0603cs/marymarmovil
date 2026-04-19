package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.presentation.viewmodel.KitchenOrdersViewModel
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import kotlinx.coroutines.delay

private val KitchenBg = Color(0xFFFCF9F1)
private val KitchenPrimary = Color(0xFF001A24)
private val KitchenMuted = Color(0xFF6F767A)
private val KitchenCard = Color.White
private val KitchenSoft = Color(0xFFF1EEE6)
private val KitchenChip = Color(0xFFEAF2F6)
private val KitchenButton = Color(0xFF3F6D84)
private val KitchenCountDark = Color(0xFF001A24)
private val KitchenCountLight = Color(0xFFD8E6EE)

@Composable
fun KitchenOrdersScreen(
    vm: KitchenOrdersViewModel
) {
    val state by vm.ui.collectAsState()

    val nowMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        vm.load()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KitchenBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Cocina",
                    modifier = Modifier.align(Alignment.Center),
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )

                Surface(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    shape = CircleShape,
                    color = KitchenCard,
                    contentColor = KitchenPrimary
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚙",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        state.message?.let { message ->
            item { InfoBanner(message) }
        }

        state.error?.let { error ->
            item { ErrorBanner(error) }
        }

        if (state.loading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            SectionHeader(
                title = "Nuevos Pedidos",
                count = state.newOrders.size,
                darkBadge = true
            )
        }

        if (state.newOrders.isEmpty()) {
            item {
                EmptyKitchenCard(text = "No hay pedidos nuevos.")
            }
        } else {
            items(state.newOrders, key = { it.id }) { order ->
                KitchenNewOrderCard(
                    order = order,
                    loading = state.actionLoadingId == order.id,
                    onStartPreparation = { vm.startPreparation(order) }
                )
            }
        }

        item {
            SectionHeader(
                title = "En Preparación",
                count = state.preparingOrders.size,
                darkBadge = false
            )
        }

        if (state.preparingOrders.isEmpty()) {
            item {
                EmptyKitchenCard(text = "No hay pedidos en preparación.")
            }
        } else {
            items(state.preparingOrders, key = { it.id }) { order ->
                val startedAt = state.preparationStartedAt[order.id]
                val frozenElapsed = state.preparationFinishedElapsed[order.id]

                val elapsedMillis = when {
                    frozenElapsed != null -> frozenElapsed
                    startedAt != null -> (nowMillis - startedAt).coerceAtLeast(0L)
                    else -> 0L
                }

                KitchenPreparingOrderCard(
                    order = order,
                    elapsedLabel = formatElapsed(elapsedMillis),
                    loading = state.actionLoadingId == order.id,
                    onMarkReady = { vm.markReady(order) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    darkBadge: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = KitchenPrimary,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        Surface(
            shape = CircleShape,
            color = if (darkBadge) KitchenCountDark else KitchenCountLight,
            contentColor = if (darkBadge) Color.White else KitchenPrimary
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyKitchenCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = KitchenSoft
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(18.dp),
            color = KitchenCard
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                color = KitchenMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun KitchenNewOrderCard(
    order: OrderResponseDto,
    loading: Boolean,
    onStartPreparation: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = KitchenCard
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Pedido #${order.id}",
                        color = KitchenPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Mesa ${order.numeroMesa ?: "--"}",
                        color = KitchenMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                StatusTag(text = "NUEVO")
            }

            ProductList(order = order)

            DarkPrimaryButton(
                text = if (loading) "Aceptando..." else "Iniciar preparación",
                enabled = !loading,
                onClick = onStartPreparation
            )
        }
    }
}

@Composable
private fun KitchenPreparingOrderCard(
    order: OrderResponseDto,
    elapsedLabel: String,
    loading: Boolean,
    onMarkReady: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = KitchenCard
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KitchenSoft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱ $elapsedLabel",
                    color = KitchenPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "EN CURSO",
                    color = KitchenMuted,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Pedido #${order.id}",
                        color = KitchenPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Mesa ${order.numeroMesa ?: "--"}",
                        color = KitchenMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ProductList(order = order)

                Surface(
                    onClick = onMarkReady,
                    shape = RoundedCornerShape(20.dp),
                    color = KitchenButton,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (loading) "Marcando..." else "MARCAR COMO LISTO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductList(order: OrderResponseDto) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        order.detalles.orEmpty().forEach { detail ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KitchenSoft
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 26.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = detail.cantidad.toString(),
                            color = KitchenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = detail.productoNombre,
                    color = KitchenPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun StatusTag(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = KitchenChip,
        contentColor = KitchenPrimary
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatElapsed(elapsedMillis: Long): String {
    val totalSeconds = (elapsedMillis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}