package com.example.classapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Navigation controls for PDF viewer
 */
@Composable
fun PDFNavigationControls(
    totalPages: Int,
    currentPage: Int,
    showToolbar: Boolean,
    onPageChange: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = totalPages > 0,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = Modifier.padding(bottom = if (showToolbar) 100.dp else 16.dp)
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentPage > 0) {
                            onPageChange(currentPage - 1)
                        }
                    },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Page"
                    )
                }

                Text(
                    text = "Page ${currentPage + 1} of $totalPages",
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(
                    onClick = {
                        if (currentPage < totalPages - 1) {
                            onPageChange(currentPage + 1)
                        }
                    },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Page"
                    )
                }
            }
        }
    }
}