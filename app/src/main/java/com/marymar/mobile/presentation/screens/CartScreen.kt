package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.presentation.viewmodel.CartViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.SoftSecondaryButton
import com.marymar.mobile.ui.components.CircleActionButton
import com.marymar.mobile.ui.components.formatMoney

private val CartBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val CartPrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val CartMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val CartCard = androidx.compose.ui.graphics.Color.White
private val CartChip = androidx.compose.ui.graphics.Color(0xFFF1EEE6)
private val CartAccent = androidx.compose.ui.graphics.Color(0xFF00303F)

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
            .background(CartBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Carrito")

        state.error?.let { ErrorBanner(it) }
        state.message?.let { InfoBanner(it) }

        if (state.items.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CartCard,
                shape = RoundedCornerShape(26.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 38.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tu carrito está vacío.",
                        color = CartMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.product.id }) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = CartCard
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        color = CartPrimary,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp
                                    )

                                    if (!item.product.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.product.description.orEmpty(),
                                            color = CartMuted,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Text(
                                    text = "$${formatMoney(item.product.price * item.quantity)}",
                                    color = CartPrimary,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircleActionButton(text = "-") {
                                    cartVm.decrease(item.product.id)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.quantity.toString(),
                                    color = CartPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                CircleActionButton(text = "+") {
                                    cartVm.increase(item.product.id)
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = CartCard
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Productos", color = CartMuted)
                    Text(cartVm.totalItems().toString(), color = CartPrimary, fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", color = CartMuted)
                    Text(
                        text = "$${formatMoney(cartVm.total())}",
                        color = CartPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }

                DarkPrimaryButton(
                    text = if (state.loading) "Confirmando..." else "Confirmar pedido",
                    enabled = state.items.isNotEmpty() && !state.loading
                ) {
                    cartVm.placeDeliveryOrder(sessionUserId)
                }

                SoftSecondaryButton(text = "Vaciar carrito") {
                    cartVm.clear()
                }
            }
        }
    }
}