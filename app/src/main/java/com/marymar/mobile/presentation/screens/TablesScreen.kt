package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.presentation.viewmodel.TablesViewModel
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.SoftSecondaryButton

private val TablesBg = Color(0xFFFCF9F1)
private val TablesPrimary = Color(0xFF001A24)
private val TablesMuted = Color(0xFF6F767A)
private val TablesCard = Color.White
private val TablesTabBg = Color(0xFFF1EEE6)
private val TablesPurple = Color(0xFF4B2A88)
private val TablesGreen = Color(0xFF9EE7B3)
private val TablesGreenText = Color(0xFF14532D)
private val TablesYellow = Color(0xFFD4C11D)
private val TablesOrange = Color(0xFFE97828)
private val TablesBlue = Color(0xFF114E63)

private fun tableStatusLabel(status: String): String = when (status.uppercase()) {
    "DISPONIBLE" -> "Disponible"
    "CREADO" -> "Creado"
    "EN_PREPARACION" -> "En preparación"
    "LISTO" -> "Listo"
    "ENTREGADO" -> "Entregado"
    "CUENTA_PEDIDA" -> "Cuenta pedida"
    else -> status.replace('_', ' ')
}

private fun tableStatusColor(status: String): Color = when (status.uppercase()) {
    "DISPONIBLE" -> TablesGreen
    "CREADO" -> TablesBlue
    "EN_PREPARACION" -> TablesPurple
    "LISTO" -> TablesYellow
    "ENTREGADO" -> Color(0xFF16A34A)
    "CUENTA_PEDIDA" -> TablesOrange
    else -> TablesTabBg
}

private fun tableTextColor(status: String): Color = when (status.uppercase()) {
    "DISPONIBLE" -> TablesGreenText
    "LISTO" -> Color(0xFF4A3F00)
    else -> Color.White
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TablesScreen(
    vm: TablesViewModel,
    meseroId: Long,
    onOpenTableOrder: (Long, Int) -> Unit
) {
    val state by vm.ui.collectAsState()
    var area by rememberSaveable { mutableStateOf("PISO_1") }

    LaunchedEffect(Unit) {
        vm.load()
    }

    val sortedTables = state.tables.sortedBy { it.numero }
    val firstFloor = sortedTables.filter { it.numero in 1..4 }
    val terrace = sortedTables.filter { it.numero in 5..8 }
    val visibleTables = if (area == "PISO_1") firstFloor else terrace

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TablesBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MinimalHeader(title = "Panel de Mesero")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AreaTab(
                text = "Primer piso",
                selected = area == "PISO_1",
                onClick = { area = "PISO_1" }
            )
            AreaTab(
                text = "Terraza",
                selected = area == "TERRAZA",
                onClick = { area = "TERRAZA" }
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = TablesCard
        ) {
            FlowRow(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "DISPONIBLE" to "Disponible",
                    "CREADO" to "Creado",
                    "EN_PREPARACION" to "En preparación",
                    "LISTO" to "Listo",
                    "ENTREGADO" to "Entregado",
                    "CUENTA_PEDIDA" to "Cuenta pedida"
                ).forEach { (status, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .background(tableStatusColor(status), CircleShape)
                                .padding(7.dp)
                        )
                        Text(
                            text = label,
                            color = TablesMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

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
            visibleTables.forEach { table ->
                TableCardApp(
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
private fun AreaTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) TablesPrimary else TablesTabBg,
        contentColor = if (selected) Color.White else TablesPrimary
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TableCardApp(
    table: TableResponseDto,
    loading: Boolean,
    onOpen: () -> Unit,
    onView: () -> Unit,
    onCancel: () -> Unit
) {
    val bg = tableStatusColor(table.estado)
    val fg = tableTextColor(table.estado)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = fg.copy(alpha = 0.18f)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = table.numero.toString(),
                        color = fg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Text(
                text = tableStatusLabel(table.estado),
                color = fg,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )

            Text(
                text = "${table.capacidad ?: 0} pax",
                color = fg.copy(alpha = 0.88f)
            )

            if (!table.meseroAsignadoNombre.isNullOrBlank()) {
                Text(
                    text = table.meseroAsignadoNombre.orEmpty(),
                    color = fg.copy(alpha = 0.88f),
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
                    ) {
                        onOpen()
                    }
                }

                else -> {
                    DarkPrimaryButton(text = "Ver pedido") { onView() }
                    SoftSecondaryButton(text = "Cancelar mesa") { onCancel() }
                }
            }
        }
    }
}