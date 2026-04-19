package com.marymar.mobile.data.repository

import com.marymar.mobile.core.network.toReadableMessage
import com.marymar.mobile.core.network.toUserFriendlyMessage
import com.marymar.mobile.core.util.ApiResult
import com.marymar.mobile.data.remote.api.OrderApi
import com.marymar.mobile.data.remote.dto.OrderCreateDto
import com.marymar.mobile.data.remote.dto.OrderDetailCreateDto
import com.marymar.mobile.data.remote.dto.OrderResponseDto
import com.marymar.mobile.data.remote.dto.PaymentResponseDto
import com.marymar.mobile.data.remote.dto.TableResponseDto
import com.marymar.mobile.domain.model.CartItem
import com.marymar.mobile.domain.model.UploadAttachment
import com.marymar.mobile.domain.repository.OrderRepository
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi
) : OrderRepository {

    override suspend fun createDeliveryOrder(
        clienteId: Long,
        items: List<CartItem>
    ): ApiResult<OrderResponseDto> {
        return try {
            val body = OrderCreateDto(
                clienteId = clienteId,
                tipo = "DOMICILIO",
                detalles = items.map {
                    OrderDetailCreateDto(
                        productoId = it.product.id,
                        cantidad = it.quantity
                    )
                }
            )
            ApiResult.Success(api.createOrder(body))
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible crear el pedido"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible crear el pedido"), null, e)
        }
    }

    override suspend fun getOrdersByClient(clienteId: Long): ApiResult<List<OrderResponseDto>> =
        callOrderList { api.getOrdersByClient(clienteId) }

    override suspend fun getAllOrders(): ApiResult<List<OrderResponseDto>> =
        callOrderList { api.getAllOrders() }

    override suspend fun getOrder(id: Long): ApiResult<OrderResponseDto> =
        callOrder { api.getOrder(id) }

    override suspend fun changeOrderStatus(orderId: Long, status: String): ApiResult<OrderResponseDto> =
        callOrder { api.updateOrderStatus(orderId, status) }

    override suspend fun getTables(): ApiResult<List<TableResponseDto>> =
        callTableList { api.getTables() }

    override suspend fun openTable(tableId: Long, meseroId: Long): ApiResult<TableResponseDto> =
        callTable { api.openTable(tableId, meseroId) }

    override suspend fun cancelTable(tableId: Long): ApiResult<TableResponseDto> =
        callTable { api.cancelTable(tableId) }

    override suspend fun closeTable(tableId: Long): ApiResult<TableResponseDto> =
        callTable { api.closeTable(tableId) }

    override suspend fun getOrderByTable(tableId: Long): ApiResult<OrderResponseDto> =
        callOrder { api.getOrderByTable(tableId) }

    override suspend fun openOrderForTable(tableId: Long, meseroId: Long): ApiResult<OrderResponseDto> =
        callOrder { api.openOrderForTable(tableId, meseroId) }

    override suspend fun addProductToOrder(orderId: Long, productId: Long, quantity: Int): ApiResult<OrderResponseDto> =
        callOrder { api.addProductToOrder(orderId, productId, quantity) }

    override suspend fun decreaseProduct(orderId: Long, productId: Long): ApiResult<OrderResponseDto> =
        callOrder { api.decreaseProduct(orderId, productId) }

    override suspend fun removeDetail(orderId: Long, detailId: Long): ApiResult<OrderResponseDto> =
        callOrder { api.removeDetail(orderId, detailId) }

    override suspend fun registerPayment(
        orderId: Long,
        method: String,
        amount: Double,
        attachment: UploadAttachment?
    ): ApiResult<PaymentResponseDto> {
        return try {
            val textPlain = "text/plain".toMediaType()
            val pedidoIdBody = orderId.toString().toRequestBody(textPlain)
            val metodoBody = method.uppercase().toRequestBody(textPlain)
            val montoBody = amount.toString().toRequestBody(textPlain)

            val comprobantePart = attachment?.let { file ->
                val mediaType = file.mimeType.ifBlank { "application/octet-stream" }.toMediaType()
                val requestBody = file.bytes.toRequestBody(mediaType)
                MultipartBody.Part.createFormData("comprobante", file.fileName, requestBody)
            }

            ApiResult.Success(
                api.registerPayment(
                    pedidoId = pedidoIdBody,
                    metodo = metodoBody,
                    monto = montoBody,
                    comprobante = comprobantePart
                )
            )
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible registrar el pago"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible registrar el pago"), null, e)
        }
    }

    private suspend fun callOrder(block: suspend () -> OrderResponseDto): ApiResult<OrderResponseDto> {
        return try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible procesar el pedido"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible procesar el pedido"), null, e)
        }
    }

    private suspend fun callTable(block: suspend () -> TableResponseDto): ApiResult<TableResponseDto> {
        return try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible actualizar la mesa"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible actualizar la mesa"), null, e)
        }
    }

    private suspend fun callOrderList(block: suspend () -> List<OrderResponseDto>): ApiResult<List<OrderResponseDto>> {
        return try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible cargar la información"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible cargar la información"), null, e)
        }
    }

    private suspend fun callTableList(block: suspend () -> List<TableResponseDto>): ApiResult<List<TableResponseDto>> {
        return try {
            ApiResult.Success(block())
        } catch (e: HttpException) {
            ApiResult.Error(e.toReadableMessage("No fue posible cargar las mesas"), e.code(), e)
        } catch (e: Exception) {
            ApiResult.Error(e.toUserFriendlyMessage("No fue posible cargar las mesas"), null, e)
        }
    }
}