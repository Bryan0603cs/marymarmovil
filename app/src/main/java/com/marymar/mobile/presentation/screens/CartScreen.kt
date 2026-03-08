package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.CartViewModel

@Composable
fun CartScreen(
    cartVm: CartViewModel,
    sessionUserId: Long,
    sessionRole: Role
) {
    val state by cartVm.ui.collectAsState()

    var clienteIdText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Carrito", style = MaterialTheme.typography.headlineSmall)

        if (sessionRole == Role.MESERO) {
            OutlinedTextField(
                value = clienteIdText,
                onValueChange = { clienteIdText = it },
                label = { Text("clienteId (para pedidos presenciales)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }

        if (state.orderCreatedId != null) {
            Text("Pedido creado ✅ ID: ${state.orderCreatedId}")
            LaunchedEffect(state.orderCreatedId) { cartVm.consumeOrderCreated() }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.items, key = { it.product.id }) { item ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                        Text("$" + String.format("%.0f", item.product.price) + " x ${item.quantity}")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { cartVm.setQty(item.product.id, item.quantity - 1) }) {
                                Text("-")
                            }
                            OutlinedButton(onClick = { cartVm.setQty(item.product.id, item.quantity + 1) }) {
                                Text("+")
                            }
                            TextButton(onClick = { cartVm.remove(item.product.id) }) {
                                Text("Quitar")
                            }
                        }
                    }
                }
            }
        }

        Text("Total: $" + String.format("%.0f", cartVm.total()))

        Button(
            onClick = {
                val clienteIdForMesero = clienteIdText.trim().toLongOrNull()
                cartVm.placeOrder(sessionUserId, sessionRole, clienteIdForMesero)
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("Crear pedido")
        }

        OutlinedButton(onClick = { cartVm.clear() }, modifier = Modifier.fillMaxWidth()) {
            Text("Vaciar carrito")
        }
    }
}
