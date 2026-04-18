package com.marymar.mobile.presentation.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marymar.mobile.BuildConfig
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

private val ActiveBg = androidx.compose.ui.graphics.Color(0xFFFCF9F1)
private val ActivePrimary = androidx.compose.ui.graphics.Color(0xFF001A24)
private val ActiveMuted = androidx.compose.ui.graphics.Color(0xFF6F767A)
private val ActiveCard = androidx.compose.ui.graphics.Color.White

@Composable
fun ActiveTableOrderScreen(
    vm: ActiveTableOrderViewModel,
    mesaId: Long,
    mesaNumero: Int,
    meseroId: Long,
    authToken: String
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    var localInfo by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(mesaId, meseroId) {
        vm.load(mesaId, meseroId)
    }

    val order = state.order

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

        localInfo?.let { message ->
            InfoBanner(message)
        }

        state.message?.let { message ->
            InfoBanner(message)
        }

        state.error?.let { error ->
            ErrorBanner(error)
        }

        if (state.loading || state.actionLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

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
                        fontSize = 20.sp
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

                    if (!order.detalles.isNullOrEmpty()) {
                        SoftSecondaryButton(text = "Generar comanda") {
                            downloadPdf(
                                context = context,
                                url = "${BuildConfig.BASE_URL.trimEnd('/')}/api/pedidos/${order.id}/comanda",
                                token = authToken,
                                fileName = "comanda_pedido_${order.id}.pdf"
                            )
                            localInfo = "Comanda descargándose..."
                        }
                    }

                    if (
                        order.estado.equals("ENTREGADO", true) ||
                        order.estado.equals("CUENTA_PEDIDA", true) ||
                        order.estado.equals("PAGADO", true)
                    ) {
                        SoftSecondaryButton(text = "Generar factura") {
                            downloadPdf(
                                context = context,
                                url = "${BuildConfig.BASE_URL.trimEnd('/')}/api/pedidos/${order.id}/factura",
                                token = authToken,
                                fileName = "factura_pedido_${order.id}.pdf"
                            )
                            localInfo = "Factura descargándose..."
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

    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}