package com.example.classapp

import android.graphics.BitmapFactory
import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WhiteboardApp(viewModel: WhiteboardViewModel) {
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf(DrawPath(mutableListOf(), Color.Blue, 5f)) }
    var color by remember { mutableStateOf(Color.Blue) }
    var strokeWidth by remember { mutableStateOf(5f) }
    var showColorPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Collect image from ViewModel
    val currentImage by viewModel.currentImage.collectAsState()

    // Track canvas dimensions
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    // Initialize paths if empty
    if (paths.isEmpty()) {
        paths.add(
            DrawPath(
                points = mutableListOf(Offset(1F, 2F)),
                color = color,
                strokeWidth = strokeWidth
            )
        )
    }

    MaterialTheme {
        Box(
            modifier = Modifier
//                .padding(vertical = 16.dp)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .onSizeChanged { size ->
                        canvasWidth = size.width.toFloat()
                        canvasHeight = size.height.toFloat()
                        viewModel.updateViewportSize(canvasWidth, canvasHeight)
                    }
            ) {
                // Background Image
                currentImage?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { motionEvent ->
                            when (motionEvent.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    currentPath = DrawPath(
                                        points = mutableListOf(Offset(motionEvent.x, motionEvent.y)),
                                        color = color,
                                        strokeWidth = strokeWidth
                                    )
                                    viewModel.sendCoordinates(
                                        motionEvent.x,
                                        motionEvent.y,
                                        true,
                                        strokeWidth,
                                        color.toArgb()
                                    )
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val newPoint = Offset(motionEvent.x, motionEvent.y)
                                    currentPath.points.add(newPoint)
                                    paths.removeAt(paths.lastIndex)
                                    paths.add(currentPath.copy())
                                    viewModel.sendCoordinates(
                                        motionEvent.x,
                                        motionEvent.y,
                                        false,
                                        strokeWidth,
                                        color.toArgb()
                                    )
                                }
                                MotionEvent.ACTION_UP -> {
                                    paths.add(currentPath)
                                    currentPath = DrawPath(mutableListOf(), color, strokeWidth)
                                }
                            }
                            true
                        }
                ) {
                    // Draw all completed paths
                    paths.forEach { drawPath ->
                        if (drawPath.points.size > 1) {
                            val path = Path().apply {
                                moveTo(drawPath.points.first().x, drawPath.points.first().y)
                                for (i in 1 until drawPath.points.size) {
                                    val p1 = drawPath.points[i - 1]
                                    val p2 = drawPath.points[i]
                                    cubicTo(
                                        p1.x, p1.y,
                                        (p1.x + p2.x) / 2, (p1.y + p2.y) / 2,
                                        p2.x, p2.y
                                    )
                                }
                            }
                            drawPath(
                                path = path,
                                color = drawPath.color,
                                style = Stroke(
                                    width = drawPath.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw current path
                    if (currentPath.points.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPath.points.first().x, currentPath.points.first().y)
                            for (i in 1 until currentPath.points.size) {
                                val p1 = currentPath.points[i - 1]
                                val p2 = currentPath.points[i]
                                cubicTo(
                                    p1.x, p1.y,
                                    (p1.x + p2.x) / 2, (p1.y + p2.y) / 2,
                                    p2.x, p2.y
                                )
                            }
                        }
                        drawPath(
                            path = path,
                            color = currentPath.color,
                            style = Stroke(
                                width = currentPath.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Floating toolbar
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)

//                    .shadow(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Gray)
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color picker button
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Color",
                            tint = color
                        )
                    }

                    // Stroke width slider
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 1f..20f,
                        modifier = Modifier.width(120.dp)
                    )

                    // Undo button
                    IconButton(
                        onClick = {
                            if (paths.size > 1) {
                                paths.removeAt(paths.lastIndex)
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Undo")
                    }

                    // Clear button
                    IconButton(
                        onClick = {
                            scope.launch {
                                paths.clear()
                                paths.add(
                                    DrawPath(
                                        points = mutableListOf(Offset(1F, 2F)),
                                        color = color,
                                        strokeWidth = strokeWidth
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            }

            // Color picker dialog
            if (showColorPicker) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(72.dp, 16.dp, 16.dp, 0.dp)
                        .shadow(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            Color.Black, Color.Blue, Color.Red, Color.Green,
                            Color(0xFF9C27B0), Color(0xFF795548), Color(0xFF2196F3),
                            Color(0xFFFF9800)
                        )
                        colors.chunked(4).forEach { rowColors ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowColors.forEach { currentColor ->
                                    val animatedColor by animateColorAsState(
                                        targetValue = if (color == currentColor)
                                            currentColor.copy(alpha = 0.7f)
                                        else currentColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(animatedColor)
                                            .border(
                                                width = if (color == currentColor) 2.dp else 1.dp,
                                                color = if (color == currentColor)
                                                    Color.Black.copy(alpha = 0.5f)
                                                else Color.Black.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            )
                                            .pointerInput(Unit) {
                                                detectDragGestures { _, _ ->
                                                    color = currentColor
                                                }
                                            }
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

data class DrawPath(
    val points: MutableList<Offset>,
    val color: Color,
    val strokeWidth: Float
)