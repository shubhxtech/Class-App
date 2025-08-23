package com.example.classapp

import AudioClientViewModel
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.classapp.ui.InitialIpDialog
import com.example.classapp.ui.theme.ClassappTheme
import com.example.classapp.ui.theme.WhiteboardWithAudio

class MainActivity : ComponentActivity() {
    private lateinit var whiteboardViewModel: WhiteboardViewModel
    private lateinit var audioViewModel: AudioClientViewModel
    private val RECORD_AUDIO_PERMISSION_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Default IP value
        val ipAddress = mutableStateOf("172.16.17.42")
       val isConnecting = mutableStateOf(false)
        val showMainContent = mutableStateOf(false)

        // Initialize ViewModels as nullable
        var whiteboardViewModel by mutableStateOf<WhiteboardViewModel?>(null)
        var audioViewModel by mutableStateOf<AudioClientViewModel?>(null)

        setContent {
            ClassappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    // Get connection state safely
                    val connectionState = whiteboardViewModel?.isConnected?.collectAsState()

                    // Monitor connection state and update UI accordingly
                    LaunchedEffect(connectionState?.value) {
                        if (connectionState?.value == true) {
                            // Only show main content after confirming connection
                            isConnecting.value = false
                            showMainContent.value = true
                        }
                    }

                    if (!showMainContent.value) {
                        // Show IP dialog until connection is confirmed
                        InitialIpDialog(
                            initialIp = ipAddress.value,
                            isLoading = isConnecting.value,
                            onIpConfirmed = { ip ->
                                ipAddress.value = ip
                                isConnecting.value = true

                                // Create ViewModels after IP confirmation
                                val viewModelFactory = AppViewModelFactory(ip)
                                whiteboardViewModel = ViewModelProvider(this, viewModelFactory)[WhiteboardViewModel::class.java]
                                audioViewModel = ViewModelProvider(this, viewModelFactory)[AudioClientViewModel::class.java]
                            },
                            onCancelConnection = {
                                isConnecting.value = false
                                // Optional: Clean up connection resources here
                            }
                        )
                    } else if (whiteboardViewModel != null && audioViewModel != null) {
                        // Show main content only after connection is confirmed
                        Box(modifier = Modifier.padding(paddingValues)) {
                            WhiteboardWithAudio(
                                whiteboardViewModel = whiteboardViewModel!!,
                                audioViewModel = audioViewModel!!
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, start the client
                onPermissionGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                // Show an explanation to the user
                showPermissionRationaleDialog()
            }
            else -> {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE
                )
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Audio recording permission is required for voice communication.")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onPermissionDenied()
            }
            .create()
            .show()
    }

    private fun onPermissionGranted() {
//        audioViewModel.startClient(applicationContext)
    }

    private fun onPermissionDenied() {
//        audioViewModel.onPermissionDenied()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        audioViewModel.stopRecording()
    }
}