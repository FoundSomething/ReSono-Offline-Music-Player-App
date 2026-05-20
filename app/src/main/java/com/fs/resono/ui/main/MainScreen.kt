package com.fs.resono.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fs.resono.ui.navigation.AppNavGraph
import com.fs.resono.ui.navigation.NavRoutes
import com.fs.resono.ui.screens.library.MiniPlayerBar
import com.fs.resono.viewmodel.PlayerViewModel


@UnstableApi
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val uiState by playerViewModel.uiState.collectAsState()

    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = backStackEntry?.destination?.route != NavRoutes.Player
    val showMiniPlayer =
        uiState.hasActiveSong && currentRoute != NavRoutes.Player

    Scaffold(
        bottomBar = {
            Column {
                if (showMiniPlayer) {
                    MiniPlayerBar(
                        uiState = uiState,
                        onPlayPause = playerViewModel::playPause,
                        onClick = {
                            navController.navigate(NavRoutes.Player) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                if (showBottomBar) {
                    BottomNavBar(navController)
                }
            }
        }
    ) { padding ->
        AppNavGraph(
            navController = navController,
            playerViewModel = playerViewModel,
            modifier = Modifier.padding(padding)
        )
    }
}
