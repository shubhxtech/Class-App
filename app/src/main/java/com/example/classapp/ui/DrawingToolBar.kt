package com.example.classapp.ui

import com.example.classapp.ToolbarContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Drawing toolbar with controls for drawing
 */
@Composable
fun DrawingToolbar(
    isVisible: Boolean,
    isLandscape: Boolean,
    canEdit: Boolean,
    color: Color,
    isErasing: Boolean,
    strokeWidth: Float,
    expandedTool: String?,
    onColorChange: (Color) -> Unit,
    onErasingChange: (Boolean) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onExpandTool: (String?) -> Unit,
    onRequestPermission: () -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = if (isLandscape) slideInVertically { -it } else slideInVertically { it },
        exit = if (isLandscape) slideOutVertically { -it } else slideOutVertically { it },
        modifier = Modifier.padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            if (isLandscape) {
                // Vertical toolbar for landscape
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canEdit) {
                        ToolbarContent(
                            isLandscape = true,
                            color = color,
                            isErasing = isErasing,
                            strokeWidth = strokeWidth,
                            expandedTool = expandedTool,
                            onColorChange = onColorChange,
                            onEraserToggle = onErasingChange,
                            onStrokeWidthChange = onStrokeWidthChange,
                            onExpandTool = onExpandTool,
                            onClear = onClear,
                            onUndo = onUndo
                        )
                    } else {
                        // Request permission button when toolbar is visible
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Request Drawing Permission")
                        }
                    }
                }
            } else {
                // Horizontal toolbar for portrait
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(72.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canEdit) {
                        ToolbarContent(
                            isLandscape = false,
                            color = color,
                            isErasing = isErasing,
                            strokeWidth = strokeWidth,
                            expandedTool = expandedTool,
                            onColorChange = onColorChange,
                            onEraserToggle = onErasingChange,
                            onStrokeWidthChange = onStrokeWidthChange,
                            onExpandTool = onExpandTool,
                            onClear = onClear,
                            onUndo = onUndo
                        )
                    } else {
                        // Request permission button when toolbar is visible
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Request Drawing Permission")
                        }
                    }
                }
            }
        }
    }
}