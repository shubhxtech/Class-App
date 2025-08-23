package com.example.classapp.ui

import AudioClientViewModel
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.classapp.AppViewModelFactory
import com.example.classapp.WhiteboardViewModel
import com.example.classapp.ui.theme.ClassappTheme
import com.example.classapp.ui.theme.WhiteboardWithAudio





@Composable
fun InitialIpDialog(
    initialIp: String,
    onIpConfirmed: (String) -> Unit,
    onCancelConnection: () -> Unit,
    isLoading: Boolean
) {
    var inputIp by remember { mutableStateOf(initialIp) }

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        title = { Text("Enter Server IP Address") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (!isLoading) {
                        "Please enter the server IP address to connect to."
                    } else {
                        "Connecting to server... Please wait while server grants permission."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputIp,
                        onValueChange = { inputIp = it },
                        label = { Text("IP Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Waiting for server permission...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isLoading) {
                    Button(
                        onClick = { onIpConfirmed(inputIp) }
                    ) {
                        Text("Connect")
                    }
                } else {
                    Button(
                        onClick = { onCancelConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    )
}
