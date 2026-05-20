package com.fs.resono.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fs.resono.data.mediastore.MediaStoreRepository

class LibraryViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext
    private val repository = MediaStoreRepository(appContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}