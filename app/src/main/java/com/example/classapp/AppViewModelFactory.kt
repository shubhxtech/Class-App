package com.example.classapp

import AudioClientViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppViewModelFactory(private val ipAddress: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WhiteboardViewModel::class.java) ->
                WhiteboardViewModel(ipAddress) as T
            modelClass.isAssignableFrom(AudioClientViewModel::class.java) ->
                AudioClientViewModel(ipAddress) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}