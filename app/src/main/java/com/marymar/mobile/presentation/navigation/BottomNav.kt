package com.marymar.mobile.presentation.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val backStack = navController.currentBackStackEntryAsState().value
    val route = backStack?.destination?.route

    val items = listOf(
        Routes.Products to "Menú",
        Routes.Profile to "Perfil"
    )

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