package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.presentation.viewmodel.TablesViewModel
import com.marymar.mobile.presentation.viewmodel.WaiterTableCard
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.formatMoney
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.delay

private val TablesBg = Color(0xFFFCF9F1)
private val TablesPrimary = Color(0xFF001A24)
private val TablesMuted = Color(0xFF6F767A)
private val TablesCard = Color.White
private val TablesSoft = Color(0xFFF1EEE6)
private val TablesSummary = Color(0xFFF3F0E8)
private val StatusDisponibleBorder = Color(0xFF9AE2B1)
private val StatusDisponibleCard = Color(0xFFF7F4EE)
private val StatusCreado = Color(0xFF144A63)
private val StatusPrep = Color(0xFF45217A)
private val StatusListo = Color(0xFFD2C118)
private val StatusEntregado = Color(0xFF179A57)
private val StatusCuenta = Color(0xFFE9722A)

private enum class AreaFilter { SALON, TERRAZA }
private enum class DashboardAction { OPEN, VIEW_ORDER, NONE }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TablesScreen(
    vm: TablesViewModel,
    meseroId: Long,
    onOpenTableOrder: (Long, Int) -> Unit
) {
    val state by vm.ui.collectAsState()
    var area by rememberSaveable { mutableStateOf(AreaFilter.SALON) }

    val currentTime by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1000)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.load()
    }

    val cards = state.cards.sortedBy { it.table.numero }

    val visibleCards = cards.filter { card ->
        when (area) {
            AreaFilter.SALON -> card.table.numero in 1..4
            AreaFilter.TERRAZA -> card.table.numero !in 1..4
        }
    }

    val freeCount = cards.count { dashboardStatus(it) == "DISPONIBLE" }
    val chargeCount = cards.count { dashboardStatus(it) == "CUENTA_PEDIDA" }

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(TablesBg)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mar y Mar",
                    color = TablesPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = (maxWidth - 12.dp) / 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.width(cardWidth),
                        title = "MESAS LIBRES",
                        value = freeCount.toString().padStart(2, '0'),
                        valueColor = TablesPrimary,
                        background = TablesCard
                    )

                    SummaryCard(
                        modifier = Modifier.width(cardWidth),
                        title = "POR COBRAR",
                        value = chargeCount.toString().padStart(2, '0'),
                        valueColor = StatusCuenta,
                        background = TablesSummary
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AreaChip(
                    text = "Salón Principal",
                    selected = area == AreaFilter.SALON,
                    onClick = { area = AreaFilter.SALON }
                )

                AreaChip(
                    text = "Terraza",
                    selected = area == AreaFilter.TERRAZA,
                    onClick = { area = AreaFilter.TERRAZA }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StatusLegend()
        }

        state.message?.let { message ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                InfoBanner(message)
            }
        }

        state.error?.let { error ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorBanner(error)
            }
        }

        if (state.loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (!state.loading && visibleCards.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = TablesCard
                ) {
                    Text(
                        text = "No hay mesas para mostrar en esta zona.",
                        modifier = Modifier.padding(24.dp),
                        color = TablesMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(
                items = visibleCards,
                key = { card -> card.table.id }
            ) { card ->
                WaiterTableDashboardCard(
                    card = card,
                    now = currentTime,
                    loading = state.actionLoadingTableId == card.table.id,
                    onPrimaryAction = {
                        when (primaryAction(card)) {
                            DashboardAction.OPEN -> vm.openTable(card.table.id, meseroId) {
                                onOpenTableOrder(card.table.id, card.table.numero)
                            }
                            DashboardAction.VIEW_ORDER -> onOpenTableOrder(card.table.id, card.table.numero)
                            DashboardAction.NONE -> Unit
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusLegend() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TablesCard
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LegendItem("Disponible", StatusDisponibleBorder)
            LegendItem("Creado", StatusCreado)
            LegendItem("En preparación", StatusPrep)
            LegendItem("Listo", StatusListo)
            LegendItem("Entregado", StatusEntregado)
            LegendItem("Cuenta pedida", StatusCuenta)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = TablesPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color,
    background: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = TablesMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.7.sp
            )
            Text(
                text = value,
                color = valueColor,
                fontFamily = FontFamily.Serif,
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AreaChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) TablesPrimary else TablesSoft,
        contentColor = if (selected) Color.White else TablesPrimary
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WaiterTableDashboardCard(
    card: WaiterTableCard,
    now: LocalDateTime,
    loading: Boolean,
    onPrimaryAction: () -> Unit
) {
    val status = dashboardStatus(card)
    val palette = paletteForStatus(status)
    val action = primaryAction(card)
    val supporting = supportingText(card, status, now)
    val detail = detailText(card, status)
    val clickableCard = action == DashboardAction.VIEW_ORDER

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .then(
                if (clickableCard) Modifier.clickable(onClick = onPrimaryAction) else Modifier
            ),
        shape = RoundedCornerShape(30.dp),
        color = palette.card,
        border = if (palette.border != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(1.5.dp, palette.border)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(palette.bubble, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card.table.numero.toString(),
                        color = palette.number,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Text(
                    text = statusLabel(status),
                    color = palette.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (supporting != null) {
                    Text(
                        text = supporting,
                        color = palette.body,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (detail != null) {
                    Text(
                        text = detail,
                        color = palette.body,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            when (action) {
                DashboardAction.OPEN -> {
                    DarkPrimaryButton(
                        text = if (loading) "Abriendo..." else "Abrir mesa",
                        enabled = !loading,
                        onClick = onPrimaryAction
                    )
                }
                DashboardAction.VIEW_ORDER, DashboardAction.NONE -> {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = palette.title
                        )
                    }
                }
            }
        }
    }
}

private data class StatusPalette(
    val card: Color,
    val bubble: Color,
    val number: Color,
    val title: Color,
    val body: Color,
    val border: Color = Color.Transparent
)

private fun dashboardStatus(card: WaiterTableCard): String {
    return when {
        !card.table.activa -> "INACTIVA"
        card.order != null -> card.order.estado.uppercase()
        card.table.estado.equals("CUENTA_PEDIDA", true) -> "CUENTA_PEDIDA"
        card.table.estado.equals("OCUPADA", true) -> "OCUPADA"
        else -> "DISPONIBLE"
    }
}

private fun primaryAction(card: WaiterTableCard): DashboardAction {
    return when (dashboardStatus(card)) {
        "DISPONIBLE" -> DashboardAction.OPEN
        "INACTIVA" -> DashboardAction.NONE
        else -> DashboardAction.VIEW_ORDER
    }
}

private fun statusLabel(status: String): String = when (status) {
    "CREADO", "CONFIRMADO" -> "CREADO"
    "EN_PREPARACION" -> "EN PREPARACIÓN"
    "LISTO" -> "LISTO"
    "ENTREGADO" -> "ENTREGADO"
    "CUENTA_PEDIDA" -> "CUENTA PEDIDA"
    "DISPONIBLE" -> "DISPONIBLE"
    "OCUPADA" -> "OCUPADA"
    "INACTIVA" -> "INACTIVA"
    else -> status.replace('_', ' ')
}

private fun paletteForStatus(status: String): StatusPalette = when (status) {
    "CREADO", "CONFIRMADO" -> StatusPalette(
        card = StatusCreado,
        bubble = Color.White.copy(alpha = 0.10f),
        number = Color.White,
        title = Color.White,
        body = Color.White.copy(alpha = 0.82f)
    )
    "EN_PREPARACION" -> StatusPalette(
        card = StatusPrep,
        bubble = Color.White.copy(alpha = 0.12f),
        number = Color.White,
        title = Color.White,
        body = Color.White.copy(alpha = 0.84f)
    )
    "LISTO" -> StatusPalette(
        card = StatusListo,
        bubble = Color.White.copy(alpha = 0.14f),
        number = TablesPrimary,
        title = Color.Black,
        body = Color.Black.copy(alpha = 0.80f)
    )
    "ENTREGADO" -> StatusPalette(
        card = StatusEntregado,
        bubble = Color.White.copy(alpha = 0.14f),
        number = Color.White,
        title = Color.White,
        body = Color.White.copy(alpha = 0.86f)
    )
    "CUENTA_PEDIDA" -> StatusPalette(
        card = StatusCuenta,
        bubble = Color.White.copy(alpha = 0.16f),
        number = Color.White,
        title = Color.White,
        body = Color.White.copy(alpha = 0.90f)
    )
    "DISPONIBLE" -> StatusPalette(
        card = StatusDisponibleCard,
        bubble = Color.White.copy(alpha = 0.95f),
        number = TablesPrimary,
        title = TablesPrimary,
        body = TablesMuted,
        border = StatusDisponibleBorder
    )
    else -> StatusPalette(
        card = TablesSummary,
        bubble = Color.White.copy(alpha = 0.95f),
        number = TablesPrimary,
        title = TablesPrimary,
        body = TablesMuted
    )
}

private fun supportingText(card: WaiterTableCard, status: String, now: LocalDateTime): String? {
    val order = card.order
    return when (status) {
        "DISPONIBLE" -> card.table.capacidad?.let { "$it pax" } ?: "Mesa libre"
        "CUENTA_PEDIDA" -> "Esperando cobro"
        "EN_PREPARACION" -> "Tiempo: ${minutesSince(order?.fecha, now)} min"
        "CREADO", "CONFIRMADO" -> "Pedido activo"
        "LISTO" -> "Listo para servir"
        "ENTREGADO" -> "Pendiente de generar factura"
        "OCUPADA" -> "Pedido en curso"
        "INACTIVA" -> "No disponible"
        else -> productPreview(order)
    }
}

private fun detailText(card: WaiterTableCard, status: String): String? {
    val order = card.order
    return when (status) {
        "DISPONIBLE" -> null
        "CUENTA_PEDIDA" -> order?.let { "Total: $${formatMoney(it.total)}" }
        "CREADO", "CONFIRMADO", "EN_PREPARACION", "LISTO", "ENTREGADO", "OCUPADA" -> productPreview(order)
        else -> null
    }
}

private fun productPreview(order: OrderResponseDto?): String? {
    val names = order?.detalles.orEmpty().map { it.productoNombre }.take(2)
    return when {
        names.isNotEmpty() -> names.joinToString(", ")
        order != null -> "Pedido activo"
        else -> null
    }
}

private fun minutesSince(rawDate: String?, now: LocalDateTime): Long {
    val date = parseDate(rawDate) ?: return 0L
    val minutes = Duration.between(date, now).toMinutes()
    return minutes.coerceAtLeast(0L)
}

private fun parseDate(rawDate: String?): LocalDateTime? {
    if (rawDate.isNullOrBlank()) return null

    return try {
        LocalDateTime.parse(rawDate)
    } catch (_: DateTimeParseException) {
        try {
            OffsetDateTime.parse(rawDate).toLocalDateTime()
        } catch (_: DateTimeParseException) {
            try {
                Instant.parse(rawDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
            } catch (_: DateTimeParseException) {
                try {
                    LocalDateTime.parse(rawDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } catch (_: DateTimeParseException) {
                    null
                }
            }
        }
    }
}