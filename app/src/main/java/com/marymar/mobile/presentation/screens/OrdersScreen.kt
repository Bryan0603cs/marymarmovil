package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    vm: OrdersViewModel,
    sessionUserId: Long,
    sessionRole: Role
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(sessionUserId, sessionRole) {
        if (sessionRole == Role.CLIENTE) {
            vm.load(sessionUserId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pedidos", style = MaterialTheme.typography.headlineSmall)

        if (sessionRole != Role.CLIENTE) {
            Text(
                "Por ahora este módulo muestra el historial del CLIENTE. " +
                    "(Si quieres, ampliamos para que el mesero vea todos los pedidos.)"
            )
            return
        }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.orders, key = { it.id }) { o ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pedido #${o.id}", style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${o.estado}")
                        Text("Total: $" + String.format("%.0f", o.total))
                        Text("Fecha: ${o.fecha}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
