package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marymar.mobile.presentation.viewmodel.CartViewModel
import com.marymar.mobile.presentation.viewmodel.ProductsViewModel
import com.marymar.mobile.ui.components.ErrorBanner

private val MenuBg = Color(0xFFFCF9F1)
private val MenuPrimary = Color(0xFF001A24)
private val MenuText = Color(0xFF1C1C17)
private val MenuMuted = Color(0xFF6F767A)
private val MenuBorder = Color(0xFF71787C)
private val MenuChipBg = Color(0xFFF6F3EB)
private val MenuCardBg = Color(0xFFFFFFFF)
private val MenuSearchBg = Color(0xFFF1EEE6)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(
    productsVm: ProductsViewModel,
    cartVm: CartViewModel,
    onOpenCart: () -> Unit
) {
    val state by productsVm.ui.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchText by rememberSaveable { mutableStateOf(state.query) }

    LaunchedEffect(Unit) {
        productsVm.load()
    }

    fun applySearch() {
        productsVm.setQuery(searchText.trim())
        focusManager.clearFocus()
    }

    val visibleProducts = state.products.filter { product ->
        val matchesCategory = state.selectedCategory == null || product.category == state.selectedCategory
        val q = state.query.trim().lowercase()
        val matchesQuery = q.isBlank() ||
                product.name.lowercase().contains(q) ||
                (product.category?.lowercase()?.contains(q) == true) ||
                (product.description?.lowercase()?.contains(q) == true)
        matchesCategory && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MenuHeader()
            }

            item {
                SearchRow(
                    value = searchText,
                    onValueChange = { searchText = it },
                    onSearch = { applySearch() }
                )
            }

            if (state.categories.isNotEmpty()) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryChip(
                            text = "Todos",
                            selected = state.selectedCategory == null,
                            onClick = { productsVm.setCategory(null) }
                        )

                        state.categories.forEach { category ->
                            CategoryChip(
                                text = category,
                                selected = state.selectedCategory == category,
                                onClick = { productsVm.setCategory(category) }
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
                                .height(420.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MenuPrimary)
                        }
                    }
                }

                state.error != null -> {
                    item {
                        ErrorBanner(state.error ?: "")
                    }
                }

                visibleProducts.isEmpty() -> {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = MenuCardBg
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 34.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay productos para mostrar con ese filtro.",
                                    color = MenuMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                else -> {
                    items(visibleProducts, key = { it.id }) { product ->
                        MenuProductCard(
                            category = product.category.orEmpty().ifBlank { "Producto" },
                            name = product.name,
                            description = product.description.orEmpty(),
                            price = product.price,
                            imageUrl = product.imageUrl,
                            onAdd = { cartVm.add(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(28.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mar y Mar",
                color = MenuPrimary,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 23.sp
            )
        }

        Text(
            text = "•",
            color = MenuPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun SearchRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = MenuText,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, MenuBorder, RoundedCornerShape(28.dp))
                        .background(MenuSearchBg, RoundedCornerShape(28.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌕",
                        color = MenuMuted,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text(
                                text = "Buscar por nombre o categoría",
                                color = MenuMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Surface(
            onClick = onSearch,
            shape = RoundedCornerShape(20.dp),
            color = MenuPrimary,
            contentColor = Color.White,
            modifier = Modifier.height(54.dp),
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Buscar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MenuPrimary else MenuChipBg,
        contentColor = if (selected) Color.White else MenuPrimary,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MenuProductCard(
    category: String,
    name: String,
    description: String,
    price: Double,
    imageUrl: String?,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MenuCardBg,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box {
            Column {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(232.dp)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = category.uppercase(),
                                color = MenuMuted,
                                fontSize = 10.sp,
                                letterSpacing = 1.1.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = name,
                                color = MenuPrimary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 23.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$${formatPrice(price)}",
                            color = MenuPrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = description,
                        color = MenuMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Surface(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
                    .size(48.dp),
                shape = CircleShape,
                color = MenuPrimary,
                contentColor = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

private fun formatPrice(value: Double): String {
    return String.format("%,.0f", value).replace(',', '.')
}