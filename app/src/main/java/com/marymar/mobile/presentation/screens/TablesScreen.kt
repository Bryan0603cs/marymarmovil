package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.presentation.viewmodel.TablesViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.PrimaryActionButton
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.components.SecondaryActionButton
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SuccessGreen

private fun tableStatusLabel(status: String): String = when (status.uppercase()) {
    "DISPONIBLE" -> "Disponible"
    "OCUPADA" -> "Ocupada"
    "CUENTA_PEDIDA" -> "Cuenta pedida"
    else -> status
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TablesScreen(
    vm: TablesViewModel,
    meseroId: Long,
    onOpenTableOrder: (Long, Int) -> Unit
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Gestión de mesas",
            subtitle = "Abre una mesa, agrega productos y controla el avance del pedido"
        )

        state.message?.let { InfoBanner(it) }
        state.error?.let { ErrorBanner(it) }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.tables.sortedBy { it.numero }.forEach { table ->
                TableCard(
                    table = table,
                    loading = state.actionLoadingTableId == table.id,
                    onOpen = { vm.openTable(table.id, meseroId) { onOpenTableOrder(table.id, table.numero) } },
                    onView = { onOpenTableOrder(table.id, table.numero) },
                    onCancel = { vm.cancelTable(table.id) }
                )
            }
        }
    }
}

@Composable
private fun TableCard(
    table: TableResponseDto,
    loading: Boolean,
    onOpen: () -> Unit,
    onView: () -> Unit,
    onCancel: () -> Unit
) {
    val accent = when (table.estado.uppercase()) {
        "DISPONIBLE" -> SuccessGreen
        "CUENTA_PEDIDA" -> AccentOrange
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mesa ${table.numero}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(tableStatusLabel(table.estado), color = accent, fontWeight = FontWeight.Bold)
            Text("Capacidad: ${table.capacidad ?: 0} personas", style = MaterialTheme.typography.bodySmall)
            if (!table.meseroAsignadoNombre.isNullOrBlank()) {
                Text("Mesero: ${table.meseroAsignadoNombre}", style = MaterialTheme.typography.bodySmall)
            }
            if (!table.activa) {
                Text("Mesa inactiva", color = MaterialTheme.colorScheme.error)
            }

            when {
                !table.activa -> Text("No disponible", style = MaterialTheme.typography.bodyMedium)
                table.estado.equals("DISPONIBLE", true) -> {
                    PrimaryActionButton(text = "Abrir mesa", loading = loading, onClick = onOpen)
                }
                else -> {
                    PrimaryActionButton(text = "Ver pedido", loading = loading, onClick = onView)
                    SecondaryActionButton(text = "Cancelar mesa", onClick = onCancel)
                }
            }
        }
    }
}
