package com.marymar.mobile.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.marymar.mobile.domain.model.Role

private val NavBg = Color(0xFFFCF9F1)
private val NavActive = Color(0xFF001A24)
private val NavMuted = Color(0x99001A24)

private data class BottomItem(
    val route: String,
    val label: String,
    val symbol: String
)

@Composable
fun BottomNavBar(
    navController: NavController,
    role: Role,
    modifier: Modifier = Modifier
) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val items = when (role) {
        Role.MESERO -> listOf(
            BottomItem(Routes.Tables, "Mesas", "▦"),
            BottomItem(Routes.Orders, "Pedidos", "▤"),
            BottomItem(Routes.Profile, "Perfil", "⚙")
        )

        Role.COCINERO -> listOf(
            BottomItem(Routes.Kitchen, "Cocina", "☰"),
            BottomItem(Routes.Profile, "Perfil", "⚙")
        )

        else -> listOf(
            BottomItem(Routes.Products, "Menú", "✕"),
            BottomItem(Routes.Cart, "Carrito", "🛒"),
            BottomItem(Routes.Orders, "Pedidos", "▤"),
            BottomItem(Routes.Profile, "Perfil", "⚙")
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            val cellWidth = maxWidth / items.size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = route == item.route

                    Box(
                        modifier = Modifier.width(cellWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            color = if (selected) NavActive else Color.Transparent,
                            contentColor = if (selected) NavBg else NavMuted,
                            shape = RoundedCornerShape(18.dp),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = item.symbol,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selected) NavBg else NavMuted,
                                    maxLines = 1
                                )

                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) NavBg else NavMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}