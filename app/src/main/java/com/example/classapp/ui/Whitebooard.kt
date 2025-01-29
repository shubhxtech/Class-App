import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.classapp.WhiteboardApp
import com.example.classapp.WhiteboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardWithAudio(
    whiteboardViewModel: WhiteboardViewModel,
    audioViewModel: AudioClientViewModel,
) {
    var showControlPanel by remember { mutableStateOf(false) }
    var showIpDialog by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf("192.168.0.109") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main whiteboard content
        WhiteboardApp(whiteboardViewModel)

        // FAB for showing/hiding control panel
        FloatingActionButton(
            onClick = { showControlPanel = !showControlPanel },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = if (showControlPanel) Icons.Rounded.Close else Icons.Rounded.Call,
                contentDescription = if (showControlPanel) "Hide Controls" else "Show Controls"
            )
        }

        // Animated Control Panel
        AnimatedVisibility(
            visible = showControlPanel,
            enter = slideInHorizontally(initialOffsetX = { it * 2 }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it * 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 16.dp)
        ) {
            AudioControlPanel(
                viewModel = audioViewModel,
                ipAddress = ipAddress,
                onIpDialogRequest = { showIpDialog = true }
            )
        }

        // IP Input Dialog
        if (showIpDialog) {
            AlertDialog(
                onDismissRequest = { showIpDialog = false },
                title = { Text("Enter Server IP") },
                text = {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showIpDialog = false }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showIpDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AudioControlPanel(
    viewModel: AudioClientViewModel,
    ipAddress: String,
    onIpDialogRequest: () -> Unit,
    current: Context = LocalContext.current
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) viewModel.onPermissionGranted()
            else viewModel.onPermissionDenied()
        }
    )

    Surface(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Header with IP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusIndicator(connectionState)
                    Text(
                        text = getStatusText(connectionState),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                IconButton(onClick = onIpDialogRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit IP"
                    )
                }
            }

            // IP Address display
            Text(
                text = "Server: $ipAddress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Error message if present
            AnimatedVisibility(
                visible = errorState != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                errorState?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Control Buttons
            when (connectionState) {
                is AudioClientViewModel.ConnectionState.Disconnected -> {
//                    FilledTonalButton(
//                        onClick = {
//                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Icon(
//                            imageVector = Icons.Rounded.Call,
//                            contentDescription = "Microphone",
//                            modifier = Modifier.size(18.dp)
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Text("Request Permission")
//                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.connect(
                                    context = current,
                                    host = ipAddress,
                                    port = 8000
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Connect",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Connect")
                    }
                }

                is AudioClientViewModel.ConnectionState.Connected -> {
                    FilledTonalButton(
                        onClick = { viewModel.startCommunication() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Start Communication")
                    }
                }

                is AudioClientViewModel.ConnectionState.Communicating -> {
                    Button(
                        onClick = { viewModel.stopCommunication() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Stop",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Stop")
                    }
                }

                is AudioClientViewModel.ConnectionState.Error -> {
                    Button(
                        onClick = { viewModel.onPermissionGranted() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Retry")
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun StatusIndicator(state: AudioClientViewModel.ConnectionState) {
    val color = when (state) {
        is AudioClientViewModel.ConnectionState.Disconnected -> MaterialTheme.colorScheme.error
        is AudioClientViewModel.ConnectionState.Connected -> MaterialTheme.colorScheme.primary
        is AudioClientViewModel.ConnectionState.Communicating -> MaterialTheme.colorScheme.tertiary
        is AudioClientViewModel.ConnectionState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

private fun getStatusText(state: AudioClientViewModel.ConnectionState): String {
    return when (state) {
        is AudioClientViewModel.ConnectionState.Disconnected -> "Disconnected"
        is AudioClientViewModel.ConnectionState.Connected -> "Connected"
        is AudioClientViewModel.ConnectionState.Communicating -> "Active"
        is AudioClientViewModel.ConnectionState.Error -> "Error"
        else -> "Unknown"
    }
}