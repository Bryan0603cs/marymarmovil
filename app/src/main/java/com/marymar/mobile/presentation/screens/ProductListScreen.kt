package com.marymar.mobile.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marymar.mobile.presentation.viewmodel.ProductsViewModel
import com.marymar.mobile.ui.components.ErrorBanner
import com.marymar.mobile.ui.components.InfoBanner
import com.marymar.mobile.ui.components.SectionHeader
import com.marymar.mobile.ui.theme.AccentOrange
import com.marymar.mobile.ui.theme.ChipBlue
import com.marymar.mobile.ui.theme.SoftBeige
import com.marymar.mobile.ui.theme.SurfaceWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(productsVm: ProductsViewModel) {
    val state by productsVm.ui.collectAsState()

    LaunchedEffect(Unit) { productsVm.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBeige)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Menú disponible",
            subtitle = "Consulta los productos activos del restaurante. La creación de pedidos quedará habilitada en la siguiente fase."
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = productsVm::setQuery,
            label = { Text("Buscar por nombre o categoría") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        if (state.categories.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { productsVm.setCategory(null) },
                    label = { Text("Todos") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedCategory == null) MaterialTheme.colorScheme.primary else SurfaceWhite,
                        labelColor = if (state.selectedCategory == null) SurfaceWhite else MaterialTheme.colorScheme.primary
                    )
                )
                state.categories.forEach { category ->
                    AssistChip(
                        onClick = { productsVm.setCategory(category) },
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedCategory == category) MaterialTheme.colorScheme.primary else SurfaceWhite,
                            labelColor = if (state.selectedCategory == category) SurfaceWhite else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        InfoBanner("Esta versión móvil ya consume el backend desplegado. Los botones de pedido se dejan visibles solo como referencia visual, pero aún no envían pedidos.")

        when {
            state.loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                ErrorBanner(state.error ?: "")
            }
            productsVm.filtered().isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos para mostrar con ese filtro.")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productsVm.filtered(), key = { it.id }) { p ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = p.imageUrl,
                                    contentDescription = p.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(p.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                            if (!p.category.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(p.category ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                        Text(
                                            text = "$${String.format("%,.0f", p.price)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AccentOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (!p.description.isNullOrBlank()) {
                                        Text(
                                            p.description ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(ChipBlue, RoundedCornerShape(999.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("Pedir próximamente", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
