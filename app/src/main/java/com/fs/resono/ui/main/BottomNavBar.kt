package com.fs.resono.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination // <-- IMPORTANT NEW IMPORT
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fs.resono.ui.navigation.NavRoutes

@Composable
fun BottomNavBar(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        // --- Library Navigation Item ---
        NavigationBarItem(
            selected = currentRoute == NavRoutes.Library,
            onClick = {
                if (currentRoute != NavRoutes.Library) {
                    navController.navigate(NavRoutes.Library) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Save the state of the screens being popped
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(Icons.Default.LibraryMusic, contentDescription = "Library")
            },
            label = { Text("Library") }
        )

        // --- Equalizer Navigation Item ---
        NavigationBarItem(
            selected = currentRoute == NavRoutes.Equlizer,
            onClick = {
                if (currentRoute != NavRoutes.Equlizer) {
                    navController.navigate(NavRoutes.Equlizer) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(Icons.Default.Equalizer, contentDescription = "Equalizer")
            },
            label = { Text("EQ") }
        )
    }
}