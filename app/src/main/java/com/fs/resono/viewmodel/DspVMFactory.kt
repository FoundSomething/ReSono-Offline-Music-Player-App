package com.fs.resono.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DspViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DspViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DspViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}