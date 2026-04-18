package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.OrderListFilter
import com.marymar.mobile.presentation.viewmodel.OrdersViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.OrderProgressTimeline
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.components.StatusChip
import com.marymar.mobile.ui.components.userFacingOrderStatus
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    vm: OrdersViewModel,
    sessionUserId: Long,
    sessionRole: Role,
    onOpenOrder: (Long) -> Unit
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(sessionUserId, sessionRole) {
        vm.load(sessionUserId, sessionRole)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = if (sessionRole == Role.MESERO) "Pedidos asignados" else "Tus pedidos",
            subtitle = if (sessionRole == Role.MESERO) {
                "Consulta el avance de los pedidos de tus mesas"
            } else {
                "Haz seguimiento a tus pedidos a domicilio"
            }
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OrderListFilter.values().forEach { filter ->
                StatusChip(
                    text = when (filter) {
                        OrderListFilter.ACTIVOS -> "Activos"
                        OrderListFilter.FINALIZADOS -> "Finalizados"
                        OrderListFilter.TODOS -> "Todos"
                    },
                    selected = state.filter == filter,
                    onClick = { vm.setFilter(filter) }
                )
            }
        }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error?.let { ErrorBanner(it) }

        val visibleOrders = vm.filteredOrders()

        if (!state.loading && visibleOrders.isEmpty()) {
            Text(
                text = if (sessionRole == Role.MESERO) {
                    "Aún no tienes pedidos asignados en este filtro."
                } else {
                    "Todavía no tienes pedidos registrados."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visibleOrders, key = { it.id }) { order ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenOrder(order.id) },
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (order.tipo.equals("MESA", true) && order.numeroMesa != null) {
                                "Pedido #${order.id} · Mesa ${order.numeroMesa}"
                            } else {
                                "Pedido #${order.id}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OrderStatusBadge(order.estado)
                        Text(
                            text = userFacingOrderStatus(order.estado),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Fecha: ${order.fecha.replace('T', ' ').take(16)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$${String.format("%,.0f", order.total)}",
                            color = AccentOrange,
                            fontWeight = FontWeight.Bold
                        )
                        OrderProgressTimeline(status = order.estado, type = order.tipo)
                    }
                }
            }
        }
    }
}
