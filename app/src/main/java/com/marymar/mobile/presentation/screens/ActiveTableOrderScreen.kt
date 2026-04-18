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
import com.marymar.mobile.presentation.viewmodel.ActiveTableOrderViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.nextMeseroActionLabel
import com.marymar.mobile.ui.components.nextMeseroStatus
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.SoftSecondaryButton
import com.marymar.mobile.ui.components.formatMoney

private val ActiveBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val ActivePrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val ActiveMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val ActiveCard = androidx.compose.ui.graphics.Color.White

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
            .background(ActiveBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Mesa $mesaNumero")

        state.message?.let { InfoBanner(it) }
        state.error?.let { ErrorBanner(it) }

        if (state.loading || state.actionLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        val order = state.order
        if (order == null && !state.loading) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ActiveCard
            ) {
                Text(
                    text = "No fue posible cargar el pedido.",
                    modifier = Modifier.padding(20.dp),
                    color = ActiveMuted
                )
            }
            return@Column
        }

        if (order != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ActiveCard
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pedido #${order.id}",
                        color = ActivePrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    OrderStatusBadge(order.estado)

                    Text(
                        text = "Total: $${formatMoney(order.total)}",
                        color = ActivePrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp
                    )

                    nextMeseroStatus(order.estado)?.let { nextStatus ->
                        DarkPrimaryButton(
                            text = if (state.actionLoading) {
                                "Actualizando..."
                            } else {
                                nextMeseroActionLabel(order.estado) ?: "Actualizar estado"
                            },
                            enabled = !state.actionLoading
                        ) {
                            vm.advanceStatus(nextStatus)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ActiveCard
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Productos",
                        color = ActivePrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )

                    if (order.detalles.isNullOrEmpty()) {
                        Text(
                            text = "Sin productos.",
                            color = ActiveMuted
                        )
                    } else {
                        order.detalles.forEach { detail ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = detail.productoNombre,
                                    color = ActivePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${detail.cantidad} unidades · $${formatMoney(detail.subtotal)}",
                                    color = ActiveMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                SoftSecondaryButton(text = "Restar") {
                                    vm.decreaseProduct(detail.productoId)
                                }
                                SoftSecondaryButton(text = "Quitar") {
                                    vm.removeDetail(detail.id)
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Agregar productos",
            color = ActivePrimary,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.products, key = { it.id }) { product ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = ActiveCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = product.name,
                            color = ActivePrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (!product.description.isNullOrBlank()) {
                            Text(
                                text = product.description.orEmpty(),
                                color = ActiveMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "$${formatMoney(product.price)}",
                            color = ActivePrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium
                        )

                        DarkPrimaryButton(
                            text = if (state.actionLoading) "Agregando..." else "Agregar",
                            enabled = !state.actionLoading
                        ) {
                            vm.addProduct(product.id)
                        }
                    }
                }
            }
        }
    }
}