package com.example.classapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.classapp.ui.DrawingToolbar
import kotlinx.coroutines.launch

/**
 * Main WhiteboardApp composable that coordinates all UI components
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WhiteboardApp(viewModel: WhiteboardViewModel) {
    // App state
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf(DrawPath(mutableListOf(), Color.Blue, 5f)) }
    var color by remember { mutableStateOf(Color.Blue) }
    var strokeWidth by remember { mutableStateOf(5f) }
    var showToolbar by remember { mutableStateOf(false) } // Default to hidden since we're using our custom panel
    var isErasing by remember { mutableStateOf(false) }
    var expandedTool by remember { mutableStateOf<String?>(null) }

    // Environment
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // Collect state from ViewModel
    val isConnected by viewModel.isConnected.collectAsState()
    val canEdit by viewModel.canEdit.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val currentImage by viewModel.currentImage.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    // Initialize empty path if needed
    if (paths.isEmpty()) {
        paths.add(DrawPath(mutableListOf(Offset(1f, 2f)), color, strokeWidth, currentPage))
    }

    // Track canvas dimensions for passing to ViewModel
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }
    var question by remember { mutableStateOf("") }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Connection Status Banner
            ConnectionStatusBanner(
                connectionStatus = connectionStatus,
                canEdit = canEdit,
                isConnected = isConnected,
                onRequestPermission = { viewModel.requestEditPermission(question) }
            )

            // Main Canvas Container
            WhiteboardCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (connectionStatus != null) 70.dp else 0.dp),
                paths = paths,
                currentPath = currentPath,
                canEdit = canEdit,
                currentImage = currentImage,
                onSizeChange = { width, height ->
                    canvasWidth = width
                    canvasHeight = height
                    viewModel.updateViewportSize(width, height)
                },
                onDrawStart = { x, y ->
                    if (canEdit) {
                        val activeColor = if (isErasing) Color.White else color
                        currentPath = DrawPath(
                            points = mutableListOf(Offset(x, y)),
                            color = activeColor,
                            strokeWidth = strokeWidth,
                            pageNumber = currentPage
                        )
                        // Send coordinates to server
                        viewModel.sendCoordinates(
                            x, y, true, strokeWidth, activeColor.toArgb()
                        )
                    }
                },
                onDrawMove = { x, y ->
                    if (canEdit) {
                        val activeColor = if (isErasing) Color.White else color
                        val newPoint = Offset(x, y)
                        currentPath.points.add(newPoint)

                        // Update local drawing
                        if (paths.isNotEmpty()) {
                            paths.removeAt(paths.lastIndex)
                        }
                        paths.add(currentPath.copy())

                        // Send to server
                        viewModel.sendCoordinates(
                            x, y, false, strokeWidth, activeColor.toArgb()
                        )
                    }
                },
                onDrawEnd = {
                    if (canEdit) {
                        paths.add(currentPath)
                        currentPath = DrawPath(
                            points = mutableListOf(),
                            color = if (isErasing) Color.White else color,
                            strokeWidth = strokeWidth,
                            pageNumber = currentPage
                        )
                    }
                }
            )

//            // PDF Navigation Controls
//            PDFNavigationControls(
//                totalPages = totalPages,
//                currentPage = currentPage,
//                showToolbar = showToolbar,
//                onPageChange = { viewModel.changePage(it) }
//            )

            // REMOVED: Toggle toolbar button FAB
            // REMOVED: Request Edit Permission Button FAB

            // Drawing Toolbar - Only show if explicitly enabled (for compatibility)
            DrawingToolbar(
                isVisible = showToolbar,
                isLandscape = isLandscape,
                canEdit = canEdit,
                color = color,
                isErasing = isErasing,
                strokeWidth = strokeWidth,
                expandedTool = expandedTool,
                onColorChange = { color = it },
                onErasingChange = { isErasing = it },
                onStrokeWidthChange = { strokeWidth = it },
                onExpandTool = { expandedTool = it },
                onRequestPermission = { viewModel.requestEditPermission(question) },
                onClear = {
                    scope.launch {
                        paths.clear()
                        paths.add(
                            DrawPath(
                                points = mutableListOf(Offset(1f, 2f)),
                                color = color,
                                strokeWidth = strokeWidth,
                                pageNumber = currentPage
                            )
                        )
                    }
                },
                onUndo = {
                    if (paths.size > 1) {
                        paths.removeAt(paths.lastIndex)
                    }
                }
            )

            // Color Picker Popup
            AnimatedVisibility(
                visible = expandedTool == "color" && canEdit,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
                    .padding(
                        bottom = if (isLandscape) 0.dp else 100.dp,
                        start = if (isLandscape) 80.dp else 0.dp
                    )
            ) {
                ColorPickerPanel(
                    currentColor = color,
                    onColorSelected = {
                        color = it
                        expandedTool = null
                        isErasing = false
                    }
                )
            }

            // Stroke Width Popup
            AnimatedVisibility(
                visible = expandedTool == "stroke" && canEdit,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
                    .padding(
                        bottom = if (isLandscape) 0.dp else 100.dp,
                        start = if (isLandscape) 80.dp else 0.dp
                    )
            ) {
                StrokeWidthPanel(
                    currentStrokeWidth = strokeWidth,
                    onStrokeWidthSelected = {
                        strokeWidth = it
                        expandedTool = null
                    }
                )
            }
        }
    }
}