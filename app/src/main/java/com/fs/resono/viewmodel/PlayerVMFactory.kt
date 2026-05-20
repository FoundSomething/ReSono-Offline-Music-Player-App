package com.fs.resono.viewmodel

import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

class PlayerViewModelFactory(
    private val application: Application, // Changed from Context to Application
    private val dspViewModel: DspViewModel
) : ViewModelProvider.Factory {

    @OptIn(UnstableApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(
                application = application, // Matches the 'application' parameter name
                dspViewModel = dspViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}