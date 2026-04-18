package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.marymar.mobile.presentation.viewmodel.OrderDetailViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.OrderProgressTimeline
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige

@Composable
fun OrderDetailScreen(
    vm: OrderDetailViewModel,
    orderId: Long
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(orderId) { vm.load(orderId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Detalle del pedido", subtitle = "Consulta productos, total y avance del proceso")

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error?.let { ErrorBanner(it) }

        val order = state.order ?: return@Column

        ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pedido #${order.id}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                OrderStatusBadge(order.estado)
                Text("Fecha: ${order.fecha.replace('T', ' ').take(16)}", style = MaterialTheme.typography.bodySmall)
                if (order.numeroMesa != null) {
                    Text("Mesa: ${order.numeroMesa}")
                }
                if (!order.clienteNombre.isNullOrBlank()) {
                    Text("Cliente: ${order.clienteNombre}")
                }
                OrderProgressTimeline(status = order.estado, type = order.tipo)
                Text("Total: $${String.format("%,.0f", order.total)}", color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(order.detalles.orEmpty(), key = { it.id }) { detail ->
                ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(detail.productoNombre, fontWeight = FontWeight.Bold)
                        Text("Cantidad: ${detail.cantidad}")
                        Text("Precio unitario: $${String.format("%,.0f", detail.precioUnitario)}")
                        Text("Subtotal: $${String.format("%,.0f", detail.subtotal)}", color = AccentOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
