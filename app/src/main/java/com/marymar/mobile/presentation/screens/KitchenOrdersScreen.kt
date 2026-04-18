package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.presentation.viewmodel.KitchenOrdersViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.formatMoney

private val KitchenBg = Color(0xFFFCF9F1)
private val KitchenPanel = Color(0xFFF6F3EB)
private val KitchenCard = Color.White
private val KitchenPrimary = Color(0xFF001A24)
private val KitchenPrimarySoft = Color(0xFF00303F)
private val KitchenMuted = Color(0xFF6F767A)
private val KitchenBorderSoft = Color(0xFFE5E2DA)
private val KitchenBlue = Color(0xFF3D6374)
private val KitchenBlueSoft = Color(0xFFD6E5ED)
private val KitchenReadyCircle = Color(0xFF6C8792)
private val KitchenCountDark = Color(0xFF001A24)
private val KitchenCountLight = Color(0xFFD6E5ED)

@Composable
fun KitchenOrdersScreen(
    vm: KitchenOrdersViewModel
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KitchenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            KitchenHeader()
        }

        if (state.loading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        state.message?.let { msg ->
            item { InfoBanner(msg) }
        }

        state.error?.let { err ->
            item { ErrorBanner(err) }
        }

        item {
            KitchenSectionContainer(
                title = "Nuevos Pedidos",
                count = state.queue.size,
                darkCount = true
            ) {
                if (state.queue.isEmpty()) {
                    EmptyKitchenCard("No hay pedidos nuevos.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.queue.forEach { order ->
                            NewKitchenOrderCard(
                                order = order,
                                loading = state.actionLoadingOrderId == order.id,
                                onAction = { vm.startPreparation(order.id) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "En Preparación",
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                CountBubble(
                    count = state.preparing.size,
                    dark = false
                )
            }
        }

        if (state.preparing.isEmpty()) {
            item { EmptyKitchenCard("No hay pedidos en preparación.") }
        } else {
            items(state.preparing, key = { it.id }) { order ->
                PreparingKitchenOrderCard(
                    order = order,
                    loading = state.actionLoadingOrderId == order.id,
                    onAction = { vm.markReady(order.id) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listos",
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Ver todos",
                    color = KitchenPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (state.ready.isEmpty()) {
            item { EmptyKitchenCard("No hay pedidos listos.") }
        } else {
            items(state.ready, key = { it.id }) { order ->
                ReadyKitchenOrderCard(order)
            }
        }
    }
}

@Composable
private fun KitchenHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "☰",
            color = KitchenPrimary,
            fontSize = 22.sp,
            modifier = Modifier.padding(end = 16.dp)
        )

        Text(
            text = "Cocina",
            color = KitchenPrimary,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "⚙",
            color = KitchenPrimary,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun KitchenSectionContainer(
    title: String,
    count: Int,
    darkCount: Boolean,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = KitchenPanel
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                CountBubble(
                    count = count,
                    dark = darkCount
                )
            }

            content()
        }
    }
}

@Composable
private fun CountBubble(
    count: Int,
    dark: Boolean
) {
    Surface(
        shape = CircleShape,
        color = if (dark) KitchenCountDark else KitchenCountLight
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = if (dark) Color.White else KitchenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyKitchenCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = KitchenCard
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            color = KitchenMuted
        )
    }
}

@Composable
private fun NewKitchenOrderCard(
    order: OrderResponseDto,
    loading: Boolean,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = KitchenCard
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido #${order.id}",
                        color = KitchenPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = kitchenDestination(order),
                        color = KitchenMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TimePill(kitchenTimeLabel(order))
            }

            KitchenItemsList(order)

            KitchenPrimaryButton(
                text = if (loading) "INICIANDO..." else "INICIAR PREPARACIÓN",
                color = KitchenPrimary,
                onClick = onAction
            )
        }
    }
}

@Composable
private fun PreparingKitchenOrderCard(
    order: OrderResponseDto,
    loading: Boolean,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = KitchenCard
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF6F3EB))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱ 12:45",
                    color = KitchenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "EN CURSO",
                    color = KitchenMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pedido #${order.id}",
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = kitchenDestination(order),
                    color = KitchenMuted,
                    style = MaterialTheme.typography.bodyMedium
                )

                KitchenItemsList(order)

                KitchenPrimaryButton(
                    text = if (loading) "ACTUALIZANDO..." else "MARCAR COMO LISTO",
                    color = KitchenBlue,
                    onClick = onAction
                )
            }
        }
    }
}

@Composable
private fun ReadyKitchenOrderCard(order: OrderResponseDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF6F3EB)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pedido #${order.id}",
                    color = KitchenPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                val itemCount = order.detalles.orEmpty().sumOf { it.cantidad }

                Text(
                    text = "${kitchenDestination(order)} • $itemCount item${if (itemCount == 1) "" else "s"}",
                    color = KitchenMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Surface(
                shape = CircleShape,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = KitchenReadyCircle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun KitchenItemsList(order: OrderResponseDto) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        order.detalles.orEmpty().forEach { detail ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF1EEE6)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = detail.cantidad.toString(),
                            color = KitchenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = detail.productoNombre,
                        color = KitchenPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePill(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE5E2DA)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = KitchenMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun KitchenPrimaryButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = color,
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier.padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

private fun kitchenDestination(order: OrderResponseDto): String {
    return when {
        order.numeroMesa != null -> "MESA ${order.numeroMesa}"
        order.tipo.equals("PARA_LLEVAR", true) || order.tipo.equals("LLEVAR", true) -> "PARA LLEVAR"
        else -> "PEDIDO"
    }
}

private fun kitchenTimeLabel(order: OrderResponseDto): String {
    return "hace ${minutesAgo(order.fecha)} min"
}

private fun minutesAgo(dateText: String?): Int {
    if (dateText.isNullOrBlank()) return 0
    return 2
}