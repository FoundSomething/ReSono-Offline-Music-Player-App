package com.fs.resono.ui.navigation

import android.app.Application
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fs.resono.ui.screens.Equlizer
import com.fs.resono.ui.screens.library.LibraryRoute
import com.fs.resono.ui.screens.player.Player
import com.fs.resono.viewmodel.DspViewModel
import com.fs.resono.viewmodel.DspViewModelFactory
import com.fs.resono.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Library,
        modifier = modifier
    ) {
        composable(NavRoutes.Library) {
            LibraryRoute(
            playerViewModel = playerViewModel,
                onMiniPlayerClick = {
                    navController.navigate(NavRoutes.Player)
                    {
                        launchSingleTop = true
                    }
                },
                showMiniPlayer = true
            )
        }

        composable(NavRoutes.Player) {
            Player(vm = playerViewModel)
        }

        composable(NavRoutes.Equlizer) {
            val context = LocalContext.current
            val dspViewModel: DspViewModel = viewModel(
                factory = DspViewModelFactory(context.applicationContext as Application)
            )
            Equlizer(vm = dspViewModel)
        }
    }
}
