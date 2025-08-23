package com.example.classapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WhiteboardViewModelFactory(private val ipAddress: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WhiteboardViewModel::class.java)) {
            return WhiteboardViewModel(ipAddress) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}