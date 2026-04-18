package com.marymar.mobile.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.marymar.mobile.domain.model.Role

private val NavBg = Color(0xFFFCF9F1)
private val NavActive = Color(0xFF001A24)
private val NavMuted = Color(0x99001A24)

@Composable
fun BottomNavBar(
    navController: NavController,
    role: Role,
    modifier: Modifier = Modifier
) {
    val backStack = navController.currentBackStackEntryAsState().value
    val route = backStack?.destination?.route

    val items = when (role) {
        Role.MESERO -> listOf(
            Triple(Routes.Tables, "Mesas", "▦"),
            Triple(Routes.Orders, "Pedidos", "≣"),
            Triple(Routes.Profile, "Perfil", "•")
        )
        else -> listOf(
            Triple(Routes.Products, "Menú", "✕"),
            Triple(Routes.Cart, "Carrito", "🛒"),
            Triple(Routes.Orders, "Pedidos", "▤"),
            Triple(Routes.Profile, "Perfil", "•")
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (destination, label, symbol) ->
                val selected = route == destination

                Surface(
                    onClick = {
                        navController.navigate(destination) {
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
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) NavBg else NavMuted
                        )
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) NavBg else NavMuted
                        )
                    }
                }
            }
        }
    }
}