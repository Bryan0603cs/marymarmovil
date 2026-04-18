package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.OrderListFilter
import com.marymar.mobile.presentation.viewmodel.OrdersViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.userFacingOrderStatus
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.FilterChip
import com.marymar.mobile.ui.components.formatMoney
private val OrdersBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val OrdersPrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val OrdersMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val OrdersCard = androidx.compose.ui.graphics.Color.White
private val OrdersChip = androidx.compose.ui.graphics.Color(0xFFF6F3EB)

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

    val visibleOrders = vm.filteredOrders()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrdersBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(
            title = if (sessionRole == Role.MESERO) "Pedidos" else "Mis pedidos"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrderListFilter.values().forEach { filter ->
                FilterChip(
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

        if (!state.loading && visibleOrders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = OrdersCard
            ) {
                Text(
                    text = if (sessionRole == Role.MESERO) {
                        "No hay pedidos en este filtro."
                    } else {
                        "Aún no tienes pedidos."
                    },
                    modifier = Modifier.padding(24.dp),
                    color = OrdersMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibleOrders.size) { index ->
                val order = visibleOrders[index]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenOrder(order.id) },
                    shape = RoundedCornerShape(24.dp),
                    color = OrdersCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (order.tipo.equals("MESA", true) && order.numeroMesa != null) {
                                "Pedido #${order.id} · Mesa ${order.numeroMesa}"
                            } else {
                                "Pedido #${order.id}"
                            },
                            color = OrdersPrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )

                        OrderStatusBadge(order.estado)

                        Text(
                            text = userFacingOrderStatus(order.estado),
                            color = OrdersPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = order.fecha.replace('T', ' ').take(16),
                            color = OrdersMuted,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "$${formatMoney(order.total)}",
                            color = OrdersPrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}