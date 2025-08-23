package com.example.classapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPickerPanel(
    currentColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black,
        Color.DarkGray,
        Color.Gray,
        Color.LightGray,
        Color(0xFF1A237E), // Deep Blue
        Color(0xFF0D47A1), // Blue
        Color(0xFF2196F3), // Light Blue
        Color(0xFF03A9F4), // Sky Blue
        Color(0xFF006064), // Teal
        Color(0xFF009688), // Green-Teal
        Color(0xFF388E3C), // Green
        Color(0xFF689F38), // Light Green
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFD32F2F), // Red
        Color(0xFFC2185B), // Pink
        Color(0xFF7B1FA2), // Purple
        Color(0xFF512DA8)  // Deep Purple
    )

    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Color",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors.chunked(5)) { rowColors ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEach { colorOption ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorOption)
                                    .border(
                                        width = if (currentColor == colorOption) 2.dp else 1.dp,
                                        color = if (currentColor == colorOption)
                                            Color.Black.copy(alpha = 0.7f)
                                        else
                                            Color.Black.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(colorOption) }
                            ) {
                                if (currentColor == colorOption) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (colorOption == Color.White || colorOption == Color.Yellow ||
                                            colorOption == Color.LightGray) Color.Black else Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StrokeWidthPanel(
    currentStrokeWidth: Float,
    onStrokeWidthSelected: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Stroke Width",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Preview of current width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentStrokeWidth.dp)
                    .clip(RoundedCornerShape(currentStrokeWidth.dp))
                    .background(Color.Black)
                    .padding(vertical = 8.dp)
            )

            // Width slider
            Slider(
                value = currentStrokeWidth,
                onValueChange = { onStrokeWidthSelected(it) },
                valueRange = 1f..25f,
                steps = 24,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Width presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(2f, 5f, 10f, 15f, 20f).forEach { width ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (currentStrokeWidth.toInt() == width.toInt())
                                Color.LightGray else Color.Transparent)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable { onStrokeWidthSelected(width) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(width.dp)
                                .clip(RoundedCornerShape(width.dp))
                                .background(Color.Black)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ToolbarItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else Color.Transparent
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
                .padding(8.dp)
        ) {
            icon()
        }

        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}


@Composable
fun ToolbarContent(
    isLandscape: Boolean,
    color: Color,
    isErasing: Boolean,
    strokeWidth: Float,
    expandedTool: String?,
    onColorChange: (Color) -> Unit,
    onEraserToggle: (Boolean) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onExpandTool: (String?) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit
) {
    if (isLandscape) {
        // Vertical layout for landscape
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Color picker button
            ToolbarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                },
                label = "Color",
                isSelected = expandedTool == "color",
                onClick = { onExpandTool(if (expandedTool == "color") null else "color") }
            )

            // Eraser button
            ToolbarItem(
                icon = { Icon(Icons.Default.Delete, contentDescription = "Eraser") },
                label = "Erase",
                isSelected = isErasing,
                onClick = {
                    onEraserToggle(!isErasing)
                    onExpandTool(null)
                }
            )

            // Undo button
            ToolbarItem(
                icon = { Icon(Icons.Default.Undo, contentDescription = "Undo") },
                label = "Undo",
                isSelected = false,
                onClick = { onUndo() }
            )

            // Clear button
            ToolbarItem(
                icon = { Icon(Icons.Default.Clear, contentDescription = "Clear") },
                label = "Clear",
                isSelected = false,
                onClick = { onClear() }
            )
        }
    } else {
        // Horizontal layout for portrait
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color picker button
            ToolbarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.Gray, CircleShape)
                    )
                },
                label = "Color",
                isSelected = expandedTool == "color",
                onClick = { onExpandTool(if (expandedTool == "color") null else "color") }
            )

            // Eraser button
            ToolbarItem(
                icon = { Icon(Icons.Default.Delete, contentDescription = "Eraser") },
                label = "Erase",
                isSelected = isErasing,
                onClick = {
                    onEraserToggle(!isErasing)
                    onExpandTool(null)
                }
            )

            // Stroke width button
            ToolbarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(strokeWidth.coerceAtMost(12f).dp)
                            .clip(RoundedCornerShape(strokeWidth.coerceAtMost(12f).dp))
                            .background(if (isErasing) Color.Black else color)
                    )
                },
                label = "Width",
                isSelected = expandedTool == "stroke",
                onClick = { onExpandTool(if (expandedTool == "stroke") null else "stroke") }
            )

            // Undo button
            ToolbarItem(
                icon = { Icon(Icons.Default.Undo, contentDescription = "Undo") },
                label = "Undo",
                isSelected = false,
                onClick = { onUndo() }
            )

            // Clear button
            ToolbarItem(
                icon = { Icon(Icons.Default.Clear, contentDescription = "Clear") },
                label = "Clear",
                isSelected = false,
                onClick = { onClear() }
            )
        }
    }
}
