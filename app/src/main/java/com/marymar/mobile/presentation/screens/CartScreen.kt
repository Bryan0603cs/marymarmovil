package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
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
import com.marymar.mobile.presentation.viewmodel.CartViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.components.SecondaryActionButton
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige

@Composable
fun CartScreen(
    cartVm: CartViewModel,
    sessionUserId: Long,
    onOrderCreated: (Long) -> Unit
) {
    val state by cartVm.ui.collectAsState()

    LaunchedEffect(state.orderCreatedId) {
        state.orderCreatedId?.let {
            onOrderCreated(it)
            cartVm.consumeOrderCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            title = "Tu carrito",
            subtitle = "Confirma el pedido a domicilio. El stock se valida antes de enviarlo."
        )

        state.error?.let { ErrorBanner(it) }
        state.message?.let { InfoBanner(it) }

        if (state.items.isEmpty()) {
            InfoBanner("Aún no has agregado productos. Desde el menú puedes ir armando tu pedido.")
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.product.id }) { item ->
                    ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text(item.product.description.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$${String.format("%,.0f", item.product.price * item.quantity)}",
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    SecondaryActionButton(text = "-", modifier = Modifier) { cartVm.decrease(item.product.id) }
                                    Text(item.quantity.toString(), style = MaterialTheme.typography.titleMedium)
                                    SecondaryActionButton(text = "+", modifier = Modifier) { cartVm.increase(item.product.id) }
                                }
                            }
                        }
                    }
                }
            }
        }

        ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Productos")
                    Text(cartVm.totalItems().toString())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total")
                    Text(
                        "$${String.format("%,.0f", cartVm.total())}",
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                InfoBanner("Pedido para domicilio. Más adelante aquí podrá integrarse la pasarela de pago.")
                PrimaryActionButton(
                    text = "Confirmar pedido",
                    loading = state.loading,
                    enabled = state.items.isNotEmpty()
                ) {
                    cartVm.placeDeliveryOrder(sessionUserId)
                }
                SecondaryActionButton(text = "Vaciar carrito") {
                    cartVm.clear()
                }
            }
        }
    }
}
