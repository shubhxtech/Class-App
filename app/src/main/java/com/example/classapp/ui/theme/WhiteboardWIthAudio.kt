package com.example.classapp.ui.theme

import AudioClientViewModel
import StatusIndicator
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.classapp.WhiteboardApp
import com.example.classapp.WhiteboardViewModel
import com.example.classapp.ui.getStatusText
import kotlinx.coroutines.launch
@Composable
fun WhiteboardWithAudio(
    whiteboardViewModel: WhiteboardViewModel,
    audioViewModel: AudioClientViewModel
) {
    // State for UI controls
    var showQuestionDialog by remember { mutableStateOf(false) }
    var activeColor by remember { mutableStateOf(Color.Black) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Audio state tracking
    val audioConnectionState by audioViewModel.connectionState.collectAsState()
    val audioErrorState by audioViewModel.errorState.collectAsState()
    val isAudioMuted = remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Whiteboard state
    val canEdit by whiteboardViewModel.canEdit.collectAsState()
    val isConnected by whiteboardViewModel.isConnected.collectAsState()

    // For audio permission
    val context = LocalContext.current
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                audioViewModel.onPermissionGranted()
                coroutineScope.launch {
                    audioViewModel.connect(
                        context = context,
                        host = audioViewModel.serverIp,
                        port = 8000
                    )
                }
            } else {
                audioViewModel.onPermissionDenied()
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Main whiteboard content
        WhiteboardApp(whiteboardViewModel)

        // Hand raise FAB
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = { showQuestionDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Rounded.PanTool,
                contentDescription = "Raise Hand"
            )
        }
        var question by remember { mutableStateOf("") }

        // Question Dialog
        if (showQuestionDialog) {
            AlertDialog(
                onDismissRequest = { showQuestionDialog = false },
                title = { Text("Ask a Question") },
                text = {
                    Column {
                        Text("Enter your question to request drawing permission:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Type your question here...") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showQuestionDialog = false
                            // Request permissions for whiteboard
                            whiteboardViewModel.requestEditPermission(question)
                            // Request microphone permission and connect audio
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    ) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuestionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Simplified Toolbar - Positioned at the bottom
        AnimatedVisibility(
            visible = canEdit && isConnected,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(24.dp)),
                tonalElevation = 8.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current color indicator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(activeColor, CircleShape)
                            .clickable { showColorPicker = !showColorPicker }
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), CircleShape)
                    )

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )

                    // Eraser tool
                    IconButton(
                        onClick = { /* Enable eraser mode */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Eraser",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Brush size
                    IconButton(
                        onClick = { /* Show brush size options */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Brush,
                            contentDescription = "Brush Size",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )

                    // Audio Button
                    when (audioConnectionState) {
                        is AudioClientViewModel.ConnectionState.Disconnected -> {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        audioViewModel.connect(
                                            context = context,
                                            host = audioViewModel.serverIp,
                                            port = 8000
                                        )
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MicOff,
                                    contentDescription = "Connect Audio",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        is AudioClientViewModel.ConnectionState.Connected -> {
                            IconButton(
                                onClick = { audioViewModel.startCommunication() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Start Audio",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        is AudioClientViewModel.ConnectionState.Communicating -> {
                            IconButton(
                                onClick = { audioViewModel.stopCommunication() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MicOff,
                                    contentDescription = "Mute Audio",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        else -> {
                            IconButton(
                                onClick = { audioViewModel.onPermissionGranted() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Retry Audio",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Color picker popover
            AnimatedVisibility(
                visible = showColorPicker,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = (-60).dp)
                    .padding(start = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colorOptions = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow)
                        colorOptions.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color, CircleShape)
                                    .clickable {
                                        activeColor = color
                                        showColorPicker = false
                                        // Set color in ViewModel
                                    }
                                    .border(
                                        width = if (color == activeColor) 2.dp else 1.dp,
                                        color = if (color == activeColor)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Permission status indicator
        AnimatedVisibility(
            visible = !canEdit && isConnected,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.HourglassEmpty,
                        contentDescription = "Waiting",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Waiting for permission...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}


