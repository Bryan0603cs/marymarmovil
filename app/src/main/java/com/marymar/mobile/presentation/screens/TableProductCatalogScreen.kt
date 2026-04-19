package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marymar.mobile.presentation.viewmodel.ActiveTableOrderViewModel
import com.marymar.mobile.ui.components.DarkPrimaryButton
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.formatMoney

private val CatalogBg = Color(0xFFFCF9F1)
private val CatalogPrimary = Color(0xFF001A24)
private val CatalogMuted = Color(0xFF6F767A)
private val CatalogCard = Color.White
private val CatalogSearchBg = Color(0xFFF1EEE6)
private val CatalogAccent = Color(0xFFE9722A)
private val CatalogChipSelected = Color(0xFF144A63)
private val CatalogChipUnselected = Color(0xFFF1EEE6)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TableProductCatalogScreen(
    vm: ActiveTableOrderViewModel,
    mesaId: Long,
    mesaNumero: Int,
    meseroId: Long,
    onBack: () -> Unit
) {
    val state by vm.ui.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchText by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var selectedCategory by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }

    LaunchedEffect(mesaId, meseroId) {
        vm.load(mesaId, meseroId)
    }

    val categories = state.products
        .mapNotNull { it.category?.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val visibleProducts = state.products.filter { product ->
        val categoryMatches = selectedCategory == null || product.category == selectedCategory
        val query = searchText.trim().lowercase()

        val searchMatches = query.isBlank() ||
                product.name.lowercase().contains(query) ||
                (product.description?.lowercase()?.contains(query) == true) ||
                (product.category?.lowercase()?.contains(query) == true)

        categoryMatches && searchMatches
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = CatalogCard,
                    contentColor = CatalogPrimary,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gestión de pedido",
                        color = CatalogPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Mesa T$mesaNumero · Agregar productos",
                        color = CatalogMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CatalogCard
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pedido actual #${state.order?.id ?: "--"}",
                        color = CatalogPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Total actual: $${formatMoney(state.order?.total ?: 0.0)}",
                        color = CatalogAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        state.message?.let { message ->
            item { InfoBanner(message) }
        }

        state.error?.let { error ->
            item { ErrorBanner(error) }
        }

        item {
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = CatalogPrimary,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CatalogSearchBg
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            if (searchText.isBlank()) {
                                Text(
                                    text = "⌕  Buscar platillo...",
                                    color = CatalogMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }

        if (categories.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryChip(
                        text = "Todos",
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )

                    categories.forEach { category ->
                        CategoryChip(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        when {
            state.loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CatalogPrimary)
                    }
                }
            }

            visibleProducts.isEmpty() -> {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = CatalogCard
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay productos para ese filtro.",
                                color = CatalogMuted
                            )
                        }
                    }
                }
            }

            else -> {
                items(
                    items = visibleProducts,
                    key = { product -> product.id }
                ) { product ->
                    var quantity by remember(product.id) { mutableIntStateOf(1) }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = CatalogCard
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!product.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = product.category.orEmpty().ifBlank { "Producto" },
                                    color = CatalogMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = product.name,
                                    color = CatalogPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!product.description.isNullOrBlank()) {
                                    Text(
                                        text = product.description.orEmpty(),
                                        color = CatalogMuted,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "$${formatMoney(product.price)}",
                                    color = CatalogAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )

                                QuantitySelector(
                                    quantity = quantity,
                                    onDecrease = { if (quantity > 1) quantity-- },
                                    onIncrease = { quantity++ }
                                )

                                DarkPrimaryButton(
                                    text = if (state.actionLoading) {
                                        "Agregando..."
                                    } else {
                                        "Agregar $quantity al pedido"
                                    },
                                    enabled = !state.actionLoading,
                                    onClick = {
                                        vm.addProduct(product.id, quantity)
                                        quantity = 1
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuantityButton(text = "-", onClick = onDecrease)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CatalogSearchBg,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quantity.toString(),
                    color = CatalogPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        QuantityButton(text = "+", onClick = onIncrease)
    }
}

@Composable
private fun QuantityButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = CatalogChipSelected,
        contentColor = Color.White,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) CatalogChipSelected else CatalogChipUnselected,
        contentColor = if (selected) Color.White else CatalogPrimary
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}