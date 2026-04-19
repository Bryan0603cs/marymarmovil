package com.marymar.mobile.presentation.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.BuildConfig
import com.marymar.mobile.domain.model.UploadAttachment
import com.marymar.mobile.presentation.viewmodel.ActiveTableOrderViewModel
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.MinimalHeader
import com.marymar.mobile.ui.components.OrderStatusBadge
import com.marymar.mobile.ui.components.SoftSecondaryButton
import com.marymar.mobile.ui.components.formatMoney
import com.marymar.mobile.ui.components.nextMeseroActionLabel
import com.marymar.mobile.ui.components.nextMeseroStatus

private val ActiveBg = Color(0xFFFCF9F1)
private val ActivePrimary = Color(0xFF001A24)
private val ActiveMuted = Color(0xFF6F767A)
private val ActiveCard = Color.White
private val ActiveSoft = Color(0xFFF1EEE6)
private val ActiveOrange = Color(0xFFE9722A)

@Composable
fun ActiveTableOrderScreen(
    vm: ActiveTableOrderViewModel,
    mesaId: Long,
    mesaNumero: Int,
    meseroId: Long,
    authToken: String,
    onOpenAddProducts: () -> Unit,
    onExitTable: () -> Unit
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current

    var paymentDialogOpen by remember { mutableStateOf(false) }

    val proofPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val attachment = context.readAttachmentFrom(uri)
            if (attachment != null) {
                vm.registerPayment(
                    method = "TRANSFERENCIA",
                    attachment = attachment,
                    onSuccess = onExitTable
                )
            } else {
                vm.clearBanner()
            }
        }
    }

    LaunchedEffect(mesaId, meseroId) {
        vm.load(mesaId, meseroId)
    }

    val order = state.order

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ActiveBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MinimalHeader(title = "Mesa T$mesaNumero")
        }

        state.message?.let { message ->
            item { InfoBanner(message) }
        }

        state.error?.let { error ->
            item { ErrorBanner(error) }
        }

        if (state.loading || state.actionLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        }

        if (order == null && !state.loading) {
            item {
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
            }
            return@LazyColumn
        }

        if (order != null) {
            val commandDownloaded = state.downloadedCommandOrders.contains(order.id)

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ActiveCard
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Pedido #${order.id}",
                            color = ActivePrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 21.sp
                        )

                        OrderStatusBadge(order.estado)

                        Text(
                            text = "Total: $${formatMoney(order.total)}",
                            color = ActivePrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )

                        if (
                            (order.estado.equals("CREADO", true) || order.estado.equals("CONFIRMADO", true)) &&
                            !order.detalles.isNullOrEmpty() &&
                            !commandDownloaded
                        ) {
                            DarkPrimaryButton(
                                text = "Hacer comanda",
                                enabled = !state.actionLoading
                            ) {
                                downloadPdf(
                                    context = context,
                                    url = "${BuildConfig.BASE_URL.trimEnd('/')}/api/pedidos/${order.id}/comanda",
                                    token = authToken,
                                    fileName = "comanda_pedido_${order.id}.pdf"
                                )
                                vm.markComandaDownloaded(order.id)
                            }
                        }

                        if (order.estado.equals("ENTREGADO", true)) {
                            DarkPrimaryButton(
                                text = "Descargar factura",
                                enabled = !state.actionLoading
                            ) {
                                downloadPdf(
                                    context = context,
                                    url = "${BuildConfig.BASE_URL.trimEnd('/')}/api/pedidos/${order.id}/factura",
                                    token = authToken,
                                    fileName = "factura_pedido_${order.id}.pdf"
                                )
                            }
                        }

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

                        DarkPrimaryButton(
                            text = "Agregar productos",
                            enabled = !state.actionLoading,
                            onClick = onOpenAddProducts
                        )

                        if (!order.estado.equals("PAGADO", true) && !order.estado.equals("CANCELADO", true)) {
                            SoftSecondaryButton(text = "Cancelar mesa") {
                                vm.cancelTable(onExitTable)
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ActiveCard
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        }
                    }
                }
            }

            items(order.detalles.orEmpty(), key = { it.id }) { detail ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = ActiveCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = detail.productoNombre,
                                    color = ActivePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "${detail.cantidad} ${if (detail.cantidad == 1) "unidad" else "unidades"} · $${formatMoney(detail.subtotal)}",
                                    color = ActiveMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Surface(
                                onClick = { vm.removeDetail(detail.id) },
                                shape = RoundedCornerShape(14.dp),
                                color = ActiveSoft,
                                contentColor = ActivePrimary,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .widthIn(min = 92.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Quitar", fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Surface(
                            onClick = { vm.decreaseProduct(detail.productoId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = ActiveSoft,
                            contentColor = ActivePrimary
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Restar", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (order.estado.equals("CUENTA_PEDIDA", true)) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = ActiveOrange,
                        contentColor = Color.White,
                        onClick = { paymentDialogOpen = true }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.actionLoading) "Procesando..." else "Proceder al pago",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (paymentDialogOpen && order != null) {
        ActivePaymentMethodDialog(
            tableNumber = mesaNumero,
            total = order.total,
            onDismiss = { paymentDialogOpen = false },
            onSelectCash = {
                paymentDialogOpen = false
                vm.registerPayment(method = "EFECTIVO", onSuccess = onExitTable)
            },
            onSelectCard = {
                paymentDialogOpen = false
                vm.registerPayment(method = "TARJETA", onSuccess = onExitTable)
            },
            onSelectTransfer = {
                paymentDialogOpen = false
                proofPicker.launch("*/*")
            }
        )
    }
}

@Composable
private fun ActivePaymentMethodDialog(
    tableNumber: Int,
    total: Double,
    onDismiss: () -> Unit,
    onSelectCash: () -> Unit,
    onSelectCard: () -> Unit,
    onSelectTransfer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ActiveBg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Cobrar mesa T$tableNumber",
                    color = ActivePrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Total a registrar: $${formatMoney(total)}",
                    color = ActiveOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivePaymentOptionCard(
                    title = "Efectivo",
                    subtitle = "Registrar el pago y liberar la mesa.",
                    onClick = onSelectCash
                )
                ActivePaymentOptionCard(
                    title = "Tarjeta",
                    subtitle = "Registrar pago con datáfono o tarjeta.",
                    onClick = onSelectCard
                )
                ActivePaymentOptionCard(
                    title = "Transferencia",
                    subtitle = "Adjunta el comprobante desde el dispositivo.",
                    onClick = onSelectTransfer
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ActivePrimary)
            }
        }
    )
}

@Composable
private fun ActivePaymentOptionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = ActiveCard
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = ActivePrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = ActiveMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun downloadPdf(
    context: Context,
    url: String,
    token: String,
    fileName: String
) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(fileName)
        .setDescription("Descargando documento")
        .setMimeType("application/pdf")
        .addRequestHeader("Authorization", "Bearer $token")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOCUMENTS,
            fileName
        )

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}

private fun Context.readAttachmentFrom(uri: Uri): UploadAttachment? {
    val mimeType = contentResolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
    val fileName = queryFileName(uri) ?: "comprobante"
    val bytes = contentResolver.openInputStream(uri)?.use { input -> input.readBytes() } ?: return null
    return UploadAttachment(fileName = fileName, mimeType = mimeType, bytes = bytes)
}

private fun Context.queryFileName(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}