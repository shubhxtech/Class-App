package com.example.classapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Banner that shows connection status and permission request button
 */
@Composable
fun ConnectionStatusBanner(
    connectionStatus: String?,
    canEdit: Boolean,
    isConnected: Boolean,
    onRequestPermission: () -> Unit
) {
    AnimatedVisibility(
        visible = connectionStatus != null,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = Modifier.fillMaxWidth()
    ) {
        connectionStatus?.let { status ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (canEdit) Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!canEdit && isConnected) {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Request Permission")
                        }
                    }
                }
            }
        }
    }
}