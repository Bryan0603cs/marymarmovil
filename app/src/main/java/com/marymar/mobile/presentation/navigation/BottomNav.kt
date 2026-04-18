package com.marymar.mobile.presentation.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.marymar.mobile.domain.model.Role

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
            Routes.Tables to "Mesas",
            Routes.Orders to "Pedidos",
            Routes.Profile to "Perfil"
        )
        else -> listOf(
            Routes.Products to "Menú",
            Routes.Cart to "Carrito",
            Routes.Orders to "Pedidos",
            Routes.Profile to "Perfil"
        )
    }

    NavigationBar(modifier = modifier) {
        items.forEach { (destination, label) ->
            NavigationBarItem(
                selected = route == destination,
                onClick = {
                    navController.navigate(destination) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(label) },
                icon = {}
            )
        }
    }
}
