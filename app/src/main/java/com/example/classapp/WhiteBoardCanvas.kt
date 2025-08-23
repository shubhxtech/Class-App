package com.example.classapp

import androidx.compose.ui.ExperimentalComposeUiApi


import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap

/**
 * Canvas component that displays the PDF background and handles drawing
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WhiteboardCanvas(
    modifier: Modifier = Modifier,
    paths: List<DrawPath>,
    currentPath: DrawPath,
    canEdit: Boolean,
    currentImage: Bitmap?,
    onSizeChange: (width: Float, height: Float) -> Unit,
    onDrawStart: (x: Float, y: Float) -> Unit,
    onDrawMove: (x: Float, y: Float) -> Unit,
    onDrawEnd: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.White)
            .onSizeChanged { size ->
                onSizeChange(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        // Dotted background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 30.dp.toPx()
            val dotRadius = 1.dp.toPx()
            val dotColor = Color(0xFFE0E0E0)

            for (x in 0..(size.width / gridSize).toInt()) {
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(x * gridSize, y * gridSize)
                    )
                }
            }
        }

        // Background PDF Image
        currentImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    // Only allow drawing if user has permission
                    if (!canEdit) return@pointerInteropFilter false

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onDrawStart(motionEvent.x, motionEvent.y)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            onDrawMove(motionEvent.x, motionEvent.y)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            onDrawEnd()
                            true
                        }
                        else -> false
                    }
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
}