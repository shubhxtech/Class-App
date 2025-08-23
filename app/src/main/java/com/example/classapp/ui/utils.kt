package com.example.classapp.ui

import AudioClientViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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

fun getStatusText(state: AudioClientViewModel.ConnectionState): String {
    return when (state) {
        is AudioClientViewModel.ConnectionState.Disconnected -> "Disconnected"
        is AudioClientViewModel.ConnectionState.Connected -> "Connected"
        is AudioClientViewModel.ConnectionState.Communicating -> "Active"
        is AudioClientViewModel.ConnectionState.Error -> "Error"
        else -> "Unknown"
    }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Transparent
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}