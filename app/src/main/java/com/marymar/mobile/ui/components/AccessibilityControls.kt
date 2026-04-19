package com.marymar.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.marymar.mobile.R

private val AccessibilityPrimary = Color(0xFF0B3140)

@Composable
fun AccessibilityFloatingControls(
    expanded: Boolean,
    fontScale: Float,
    highContrast: Boolean,
    onToggleExpanded: () -> Unit,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onToggleContrast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Accesibilidad",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ElevatedAssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("Texto ${(fontScale * 100).toInt()}%") }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(
                            onClick = onDecreaseFont,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("A-")
                        }

                        ElevatedButton(
                            onClick = onIncreaseFont,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("A+")
                        }
                    }

                    ElevatedButton(
                        onClick = onToggleContrast,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (highContrast) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (highContrast) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    ) {
                        Text(
                            text = if (highContrast) {
                                "Modo alto contraste: activo"
                            } else {
                                "Activar modo alto contraste"
                            }
                        )
                    }

                    TextButton(onClick = onToggleExpanded) {
                        Text("Cerrar")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = AccessibilityPrimary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_accessibility_custom),
                contentDescription = "Accesibilidad",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}