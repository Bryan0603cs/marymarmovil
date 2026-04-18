package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marymar.mobile.presentation.viewmodel.ActiveTableOrderViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.OrderProgressTimeline
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.components.SecondaryActionButton
import com.marymar.mobile.ui.components.nextMeseroActionLabel
import com.marymar.mobile.ui.components.nextMeseroStatus
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveTableOrderScreen(
    vm: ActiveTableOrderViewModel,
    mesaId: Long,
    mesaNumero: Int,
    meseroId: Long
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(mesaId, meseroId) {
        vm.load(mesaId, meseroId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Mesa $mesaNumero",
            subtitle = "Gestiona el pedido activo, agrega productos y actualiza el estado"
        )

        state.message?.let { InfoBanner(it) }
        state.error?.let { ErrorBanner(it) }

        if (state.loading || state.actionLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        val order = state.order
        if (order == null && !state.loading) {
            Text("No fue posible cargar el pedido de la mesa.")
            return@Column
        }

        if (order != null) {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        OrderStatusBadge(order.estado)
                    }
                    Text("Total actual: $${String.format("%,.0f", order.total)}", color = AccentOrange, fontWeight = FontWeight.Bold)
                    OrderProgressTimeline(status = order.estado, type = order.tipo)
                    nextMeseroStatus(order.estado)?.let { nextStatus ->
                        PrimaryActionButton(
                            text = nextMeseroActionLabel(order.estado) ?: "Actualizar estado",
                            loading = state.actionLoading,
                            onClick = { vm.advanceStatus(nextStatus) }
                        )
                    } ?: InfoBanner("Cuando el pedido llegue a cuenta solicitada, el pago podrá registrarse desde el flujo administrativo actual.")
                }
            }

            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Productos del pedido", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    if (order.detalles.isNullOrEmpty()) {
                        Text("Aún no has agregado productos a esta mesa.")
                    } else {
                        order.detalles.forEach { detail ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(detail.productoNombre, fontWeight = FontWeight.SemiBold)
                                    Text("${detail.cantidad} unidades · $${String.format("%,.0f", detail.subtotal)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SecondaryActionButton(text = "-", modifier = Modifier) { vm.decreaseProduct(detail.productoId) }
                                    SecondaryActionButton(text = "Quitar", modifier = Modifier) { vm.removeDetail(detail.id) }
                                }
                            }
                        }
                    }
                }
            }
        }

        Text("Agregar productos", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.products, key = { it.id }) { product ->
                ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.description.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$${String.format("%,.0f", product.price)}", color = AccentOrange, fontWeight = FontWeight.Bold)
                            PrimaryActionButton(text = "Agregar", modifier = Modifier, loading = state.actionLoading) { vm.addProduct(product.id) }
                        }
                    }
                }
            }
        }
    }
}
