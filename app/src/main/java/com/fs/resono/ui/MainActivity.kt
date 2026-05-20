package com.fs.resono.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.fs.resono.ui.main.MainScreen
import com.fs.resono.ui.theme.ReSonoTheme
import com.fs.resono.viewmodel.DspViewModel
import com.fs.resono.viewmodel.PlayerViewModel
import com.fs.resono.viewmodel.PlayerViewModelFactory

class MainActivity : ComponentActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReSonoTheme{
                val dspViewModel: DspViewModel = viewModel()

                val playerViewModel: PlayerViewModel = viewModel(
                    factory = PlayerViewModelFactory(
                        application = application,
                        dspViewModel = dspViewModel
                    )
                )
                MainScreen(playerViewModel = playerViewModel)
            }
        }
    }
}
