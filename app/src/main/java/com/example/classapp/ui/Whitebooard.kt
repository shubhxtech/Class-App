import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun AudioControlPanel(
    viewModel: AudioClientViewModel,
    onIpDialogRequest: (() -> Unit)? = null
) {
    // Get IP from the ViewModel
    val ipAddress = viewModel.serverIp
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

                // Only show edit button if handler is provided
                if (onIpDialogRequest != null) {
                    IconButton(onClick = onIpDialogRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit IP"
                        )
                    }
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
            val current = LocalContext.current
            // Control Buttons
            when (connectionState) {
                is AudioClientViewModel.ConnectionState.Disconnected -> {
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
fun StatusIndicator(state: AudioClientViewModel.ConnectionState) {
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