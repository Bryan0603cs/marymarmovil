package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.marymar.mobile.presentation.viewmodel.OrderDetailViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.formatMoney
private val DetailBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val DetailPrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val DetailMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val DetailCard = androidx.compose.ui.graphics.Color.White

@Composable
fun OrderDetailScreen(
    vm: OrderDetailViewModel,
    orderId: Long
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(orderId) {
        vm.load(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Detalle")

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error?.let { ErrorBanner(it) }

        val order = state.order ?: return@Column

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = DetailCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pedido #${order.id}",
                    color = DetailPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )

                OrderStatusBadge(order.estado)

                Text(
                    text = order.fecha.replace('T', ' ').take(16),
                    color = DetailMuted,
                    style = MaterialTheme.typography.bodySmall
                )

                order.numeroMesa?.let {
                    Text("Mesa $it", color = DetailPrimary, style = MaterialTheme.typography.bodyMedium)
                }

                if (!order.clienteNombre.isNullOrBlank()) {
                    Text(order.clienteNombre, color = DetailPrimary, style = MaterialTheme.typography.bodyMedium)
                }

                Text(
                    text = "Total: $${formatMoney(order.total)}",
                    color = DetailPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(order.detalles.orEmpty(), key = { it.id }) { detail ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = DetailCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = detail.productoNombre,
                            color = DetailPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Cantidad: ${detail.cantidad}",
                            color = DetailMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Unitario: $${formatMoney(detail.precioUnitario)}",
                            color = DetailMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Subtotal: $${formatMoney(detail.subtotal)}",
                            color = DetailPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
