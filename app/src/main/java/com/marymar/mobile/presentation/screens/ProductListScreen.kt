package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marymar.mobile.presentation.viewmodel.ProductsViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(productsVm: ProductsViewModel) {
    val state by productsVm.ui.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchText by rememberSaveable { mutableStateOf("") }
    var appliedQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        productsVm.load()
    }

    val visibleProducts = state.products.filter { product ->
        val matchesCategory = state.selectedCategory == null ||
                product.category == state.selectedCategory

        val query = appliedQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                product.name.lowercase().contains(query) ||
                (product.category?.lowercase()?.contains(query) == true) ||
                (product.description?.lowercase()?.contains(query) == true)

        matchesCategory && matchesQuery
    }

    fun applySearch() {
        appliedQuery = searchText.trim()
        productsVm.setQuery(appliedQuery)
        focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(title = "Menú disponible")

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Buscar por nombre o categoría") },
            trailingIcon = {
                TextButton(onClick = { applySearch() }) {
                    Text("🔎")
                }
            }
        )

        if (state.categories.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { productsVm.setCategory(null) },
                    label = { Text("Todos") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedCategory == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            SurfaceWhite
                        },
                        labelColor = if (state.selectedCategory == null) {
                            SurfaceWhite
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                )

                state.categories.forEach { category ->
                    AssistChip(
                        onClick = { productsVm.setCategory(category) },
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedCategory == category) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                SurfaceWhite
                            },
                            labelColor = if (state.selectedCategory == category) {
                                SurfaceWhite
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    )
                }
            }
        }

        when {
            state.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                ErrorBanner(state.error ?: "")
            }

            visibleProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos para mostrar con ese filtro.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleProducts, key = { it.id }) { product ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (!product.category.isNullOrBlank()) {
                                        Text(
                                            text = product.category.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    if (!product.description.isNullOrBlank()) {
                                        Text(
                                            text = product.description.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "$${String.format("%,.0f", product.price)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AccentOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}