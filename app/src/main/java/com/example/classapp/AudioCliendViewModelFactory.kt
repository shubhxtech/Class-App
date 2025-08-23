package com.example.classapp

import AudioClientViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AudioClientViewModelFactory(private val ipAddress: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioClientViewModel::class.java)) {
            return AudioClientViewModel(ipAddress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}