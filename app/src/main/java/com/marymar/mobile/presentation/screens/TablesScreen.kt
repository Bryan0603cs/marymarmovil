package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
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
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.presentation.viewmodel.TablesViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.SoftSecondaryButton


private val TablesBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val TablesPrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val TablesMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val TablesCard = androidx.compose.ui.graphics.Color.White
private val TablesSuccess = androidx.compose.ui.graphics.Color(0xFF2E7D32)
private val TablesWarn = androidx.compose.ui.graphics.Color(0xFF9A5A00)

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

    LaunchedEffect(Unit) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TablesBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Mesas")

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
                TableCardMinimal(
                    table = table,
                    loading = state.actionLoadingTableId == table.id,
                    onOpen = {
                        vm.openTable(table.id, meseroId) {
                            onOpenTableOrder(table.id, table.numero)
                        }
                    },
                    onView = { onOpenTableOrder(table.id, table.numero) },
                    onCancel = { vm.cancelTable(table.id) }
                )
            }
        }
    }
}

@Composable
private fun TableCardMinimal(
    table: TableResponseDto,
    loading: Boolean,
    onOpen: () -> Unit,
    onView: () -> Unit,
    onCancel: () -> Unit
) {
    val accent = when (table.estado.uppercase()) {
        "DISPONIBLE" -> TablesSuccess
        "CUENTA_PEDIDA" -> TablesWarn
        else -> TablesPrimary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = TablesCard
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mesa ${table.numero}",
                color = TablesPrimary,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )

            Text(
                text = tableStatusLabel(table.estado),
                color = accent,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Capacidad ${table.capacidad ?: 0}",
                color = TablesMuted,
                style = MaterialTheme.typography.bodySmall
            )

            if (!table.meseroAsignadoNombre.isNullOrBlank()) {
                Text(
                    text = table.meseroAsignadoNombre.orEmpty(),
                    color = TablesMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            when {
                !table.activa -> {
                    Text("Mesa inactiva", color = MaterialTheme.colorScheme.error)
                }

                table.estado.equals("DISPONIBLE", true) -> {
                    DarkPrimaryButton(
                        text = if (loading) "Abriendo..." else "Abrir mesa",
                        enabled = !loading
                    ) { onOpen() }
                }

                else -> {
                    DarkPrimaryButton(text = "Ver pedido") { onView() }
                    SoftSecondaryButton(text = "Cancelar mesa") { onCancel() }
                }
            }
        }
    }
}