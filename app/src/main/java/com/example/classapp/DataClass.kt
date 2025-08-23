package com.example.classapp


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Represents a drawing path on the whiteboard
 */
data class DrawPath(
    val points: MutableList<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val pageNumber: Int = 0
)

/**
 * Represents position and scale of the displayed PDF image
 */
data class ImagePlacement(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scaleFactor: Float = 1f
)