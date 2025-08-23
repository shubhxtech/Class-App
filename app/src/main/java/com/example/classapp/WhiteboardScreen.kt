package com.example.classapp

import android.graphics.BitmapFactory
import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
//@Composable
//fun WhiteboardApp(viewModel: WhiteboardViewModel) {
//    val paths = remember { mutableStateListOf<DrawPath>() }
//    var currentPath by remember { mutableStateOf(DrawPath(mutableListOf(), Color.Blue, 5f)) }
//    var color by remember { mutableStateOf(Color.Blue) }
//    var strokeWidth by remember { mutableStateOf(5f) }
//    var showToolbar by remember { mutableStateOf(true) }
//    var showColorPicker by remember { mutableStateOf(false) }
//    var isErasing by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()
//    val configuration = LocalConfiguration.current
//    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
//
//    // UI state
//    var expandedTool by remember { mutableStateOf<String?>(null) }
//
//    // Collect state from ViewModel
//    val isConnected by viewModel.isConnected.collectAsState()
//    val canEdit by viewModel.canEdit.collectAsState()
//    val connectionStatus by viewModel.connectionStatus.collectAsState()
//    val currentImage by viewModel.currentImage.collectAsState()
//    val imageSize by viewModel.imageSize.collectAsState()
//
//    // PDF handling
//    val currentPdf by viewModel.currentPdf.collectAsState()
//    val totalPages by viewModel.totalPages.collectAsState()
//    val currentPage by viewModel.currentPage.collectAsState()
//
//    // Track canvas dimensions
//    var canvasWidth by remember { mutableStateOf(0f) }
//    var canvasHeight by remember { mutableStateOf(0f) }
//
//    // Calculate image placement
//    var imageOffsetX by remember { mutableStateOf(0f) }
//    var imageOffsetY by remember { mutableStateOf(0f) }
//    var imageScaleFactor by remember { mutableStateOf(1f) }
//
//    // Initialize paths if empty
//    if (paths.isEmpty()) {
//        paths.add(
//            DrawPath(
//                points = mutableListOf(Offset(1F, 2F)),
//                color = color,
//                strokeWidth = strokeWidth
//            )
//        )
//    }
//
//    // Calculate image placement when image size or canvas changes
//    LaunchedEffect(key1 = imageSize, key2 = canvasWidth, key3 = canvasHeight) {
//        imageSize?.let { (imgWidth, imgHeight) ->
//            if (canvasWidth > 0 && canvasHeight > 0 && imgWidth > 0 && imgHeight > 0) {
//                // Calculate aspect ratios
//                val imageAspect = imgWidth / imgHeight
//                val canvasAspect = canvasWidth / canvasHeight
//
//                // Calculate scale factor and offsets
//                if (imageAspect > canvasAspect) {
//                    // Image is wider than canvas (relative to height)
//                    imageScaleFactor = canvasWidth / imgWidth
//                    imageOffsetX = 0f
//                    imageOffsetY = (canvasHeight - (imgHeight * imageScaleFactor)) / 2f
//                } else {
//                    // Image is taller than canvas (relative to width)
//                    imageScaleFactor = canvasHeight / imgHeight
//                    imageOffsetX = (canvasWidth - (imgWidth * imageScaleFactor)) / 2f
//                    imageOffsetY = 0f
//                }
//            }
//        }
//    }
//
//    val activeColor = if (isErasing) Color.White else color
//
//    MaterialTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFFF5F5F5))
//        ) {
//            // Connection Status Banner
//            AnimatedVisibility(
//                visible = connectionStatus != null,
//                enter = slideInVertically { -it },
//                exit = slideOutVertically { -it },
//                modifier = Modifier.align(Alignment.TopCenter)
//            ) {
//                connectionStatus?.let { status ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = if (canEdit) Color(0xFF4CAF50) else Color(0xFF2196F3)
//                        )
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(12.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = status,
//                                color = Color.White,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//
//                            if (!canEdit && isConnected) {
//                                Button(
//                                    onClick = { viewModel.requestEditPermission() },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color.White,
//                                        contentColor = Color(0xFF2196F3)
//                                    )
//                                ) {
//                                    Text("Request Permission")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Main Canvas Container with Grid Background
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(top = if (connectionStatus != null) 70.dp else 0.dp)
//                    .background(Color.White)
//                    .onSizeChanged { size ->
//                        canvasWidth = size.width.toFloat()
//                        canvasHeight = size.height.toFloat()
//                        viewModel.updateViewportSize(canvasWidth, canvasHeight)
//                    }
//            ) {
//                // Dotted background grid
//                Canvas(modifier = Modifier.fillMaxSize()) {
//                    val gridSize = 30.dp.toPx()
//                    val dotRadius = 1.dp.toPx()
//                    val dotColor = Color(0xFFE0E0E0)
//
//                    for (x in 0..(size.width / gridSize).toInt()) {
//                        for (y in 0..(size.height / gridSize).toInt()) {
//                            drawCircle(
//                                color = dotColor,
//                                radius = dotRadius,
//                                center = Offset(x * gridSize, y * gridSize)
//                            )
//                        }
//                    }
//                }
//
//                // Background Image
//                currentImage?.let { bitmap ->
//                    Image(
//                        bitmap = bitmap.asImageBitmap(),
//                        contentDescription = "Background",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//
//                // Drawing Canvas
//                Canvas(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .pointerInteropFilter { motionEvent ->
//                            // Only allow drawing if user has permission
//                            if (!canEdit) return@pointerInteropFilter false
//
//                            when (motionEvent.action) {
//                                MotionEvent.ACTION_DOWN -> {
//                                    currentPath = DrawPath(
//                                        points = mutableListOf(Offset(motionEvent.x, motionEvent.y)),
//                                        color = activeColor,
//                                        strokeWidth = strokeWidth
//                                    )
//                                    viewModel.sendCoordinates(
//                                        motionEvent.x,
//                                        motionEvent.y,
//                                        true,
//                                        strokeWidth,
//                                        activeColor.toArgb()
//                                    )
//                                    true
//                                }
//                                MotionEvent.ACTION_MOVE -> {
//                                    val newPoint = Offset(motionEvent.x, motionEvent.y)
//                                    currentPath.points.add(newPoint)
//
//                                    // Update paths list for local drawing
//                                    if (paths.isNotEmpty()) {
//                                        paths.removeAt(paths.lastIndex)
//                                    }
//                                    paths.add(currentPath.copy())
//
//                                    // Send to server
//                                    viewModel.sendCoordinates(
//                                        motionEvent.x,
//                                        motionEvent.y,
//                                        false,
//                                        strokeWidth,
//                                        activeColor.toArgb()
//                                    )
//                                    true
//                                }
//                                MotionEvent.ACTION_UP -> {
//                                    paths.add(currentPath)
//                                    currentPath = DrawPath(mutableListOf(), activeColor, strokeWidth)
//                                    true
//                                }
//                                else -> false
//                            }
//                        }
//                ) {
//                    // Draw all completed paths
//                    paths.forEach { drawPath ->
//                        if (drawPath.points.size > 1) {
//                            val path = Path().apply {
//                                moveTo(drawPath.points.first().x, drawPath.points.first().y)
//                                for (i in 1 until drawPath.points.size) {
//                                    val p1 = drawPath.points[i - 1]
//                                    val p2 = drawPath.points[i]
//                                    cubicTo(
//                                        p1.x, p1.y,
//                                        (p1.x + p2.x) / 2, (p1.y + p2.y) / 2,
//                                        p2.x, p2.y
//                                    )
//                                }
//                            }
//                            drawPath(
//                                path = path,
//                                color = drawPath.color,
//                                style = Stroke(
//                                    width = drawPath.strokeWidth,
//                                    cap = StrokeCap.Round,
//                                    join = StrokeJoin.Round
//                                )
//                            )
//                        }
//                    }
//
//                    // Draw current path
//                    if (currentPath.points.size > 1) {
//                        val path = Path().apply {
//                            moveTo(currentPath.points.first().x, currentPath.points.first().y)
//                            for (i in 1 until currentPath.points.size) {
//                                val p1 = currentPath.points[i - 1]
//                                val p2 = currentPath.points[i]
//                                cubicTo(
//                                    p1.x, p1.y,
//                                    (p1.x + p2.x) / 2, (p1.y + p2.y) / 2,
//                                    p2.x, p2.y
//                                )
//                            }
//                        }
//                        drawPath(
//                            path = path,
//                            color = currentPath.color,
//                            style = Stroke(
//                                width = currentPath.strokeWidth,
//                                cap = StrokeCap.Round,
//                                join = StrokeJoin.Round
//                            )
//                        )
//                    }
//                }
//            }
//
//            // PDF Navigation Controls
//            AnimatedVisibility(
//                visible = totalPages > 0,
//                enter = fadeIn() + slideInVertically { it },
//                exit = fadeOut() + slideOutVertically { it },
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = if (showToolbar) 100.dp else 16.dp)
//            ) {
//                Card(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
//                        .clip(RoundedCornerShape(16.dp)),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.White
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .padding(8.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        IconButton(
//                            onClick = {
//                                if (currentPage > 0) {
//                                    viewModel.changePage(currentPage - 1)
//                                }
//                            },
//                            enabled = currentPage > 0
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Previous Page"
//                            )
//                        }
//
//                        Text(
//                            text = "Page ${currentPage + 1} of $totalPages",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//
//                        IconButton(
//                            onClick = {
//                                if (currentPage < totalPages - 1) {
//                                    viewModel.changePage(currentPage + 1)
//                                }
//                            },
//                            enabled = currentPage < totalPages - 1
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowForward,
//                                contentDescription = "Next Page"
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Toggle toolbar button (fixed position)
//            FloatingActionButton(
//                onClick = { showToolbar = !showToolbar },
//                modifier = Modifier
//                    .align(if (isLandscape) Alignment.TopEnd else Alignment.BottomEnd)
//                    .padding(16.dp),
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = Color.White
//            ) {
//                Icon(
//                    imageVector = if (showToolbar) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
//                    contentDescription = if (showToolbar) "Hide Toolbar" else "Show Toolbar"
//                )
//            }
//
//            // Request Edit Permission Button (when not approved)
//            if (!canEdit && !showToolbar && isConnected) {
//                FloatingActionButton(
//                    onClick = { viewModel.requestEditPermission() },
//                    modifier = Modifier
//                        .align(if (isLandscape) Alignment.BottomEnd else Alignment.BottomStart)
//                        .padding(16.dp),
//                    containerColor = Color(0xFF2196F3),
//                    contentColor = Color.White
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Edit,
//                        contentDescription = "Request Drawing Permission"
//                    )
//                }
//            }
//
//            // Responsive toolbar based on orientation
//            AnimatedVisibility(
//                visible = showToolbar,
//                enter = if (isLandscape) slideInVertically { -it } else slideInVertically { it },
//                exit = if (isLandscape) slideOutVertically { -it } else slideOutVertically { it },
//                modifier = Modifier.align(
//                    if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter
//                )
//            ) {
//                Card(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
//                        .clip(RoundedCornerShape(24.dp)),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.White
//                    )
//                ) {
//                    if (isLandscape) {
//                        // Vertical toolbar for landscape
//                        Column(
//                            modifier = Modifier
//                                .padding(vertical = 16.dp, horizontal = 8.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            // Only show drawing tools if user has permission
//                            if (canEdit) {
//                                ToolbarContent(
//                                    isLandscape = true,
//                                    color = color,
//                                    isErasing = isErasing,
//                                    strokeWidth = strokeWidth,
//                                    expandedTool = expandedTool,
//                                    onColorChange = { color = it },
//                                    onEraserToggle = { isErasing = it },
//                                    onStrokeWidthChange = { strokeWidth = it },
//                                    onExpandTool = { expandedTool = it },
//                                    onClear = {
//                                        scope.launch {
//                                            paths.clear()
//                                            paths.add(
//                                                DrawPath(
//                                                    points = mutableListOf(Offset(1F, 2F)),
//                                                    color = color,
//                                                    strokeWidth = strokeWidth
//                                                )
//                                            )
//                                        }
//                                    },
//                                    onUndo = {
//                                        if (paths.size > 1) {
//                                            paths.removeAt(paths.lastIndex)
//                                        }
//                                    }
//                                )
//                            } else {
//                                // Request permission button when toolbar is visible
//                                Button(
//                                    onClick = { viewModel.requestEditPermission() },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color(0xFF2196F3)
//                                    )
//                                ) {
//                                    Text("Request Drawing Permission")
//                                }
//                            }
//                        }
//                    } else {
//                        // Horizontal toolbar for portrait
//                        Row(
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .height(72.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            // Only show drawing tools if user has permission
//                            if (canEdit) {
//                                ToolbarContent(
//                                    isLandscape = false,
//                                    color = color,
//                                    isErasing = isErasing,
//                                    strokeWidth = strokeWidth,
//                                    expandedTool = expandedTool,
//                                    onColorChange = { color = it },
//                                    onEraserToggle = { isErasing = it },
//                                    onStrokeWidthChange = { strokeWidth = it },
//                                    onExpandTool = { expandedTool = it },
//                                    onClear = {
//                                        scope.launch {
//                                            paths.clear()
//                                            paths.add(
//                                                DrawPath(
//                                                    points = mutableListOf(Offset(1F, 2F)),
//                                                    color = color,
//                                                    strokeWidth = strokeWidth
//                                                )
//                                            )
//                                        }
//                                    },
//                                    onUndo = {
//                                        if (paths.size > 1) {
//                                            paths.removeAt(paths.lastIndex)
//                                        }
//                                    }
//                                )
//                            } else {
//                                // Request permission button when toolbar is visible
//                                Button(
//                                    onClick = { viewModel.requestEditPermission() },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color(0xFF2196F3)
//                                    )
//                                ) {
//                                    Text("Request Drawing Permission")
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Color Picker Popup
//            AnimatedVisibility(
//                visible = expandedTool == "color" && canEdit,
//                enter = fadeIn() + scaleIn(),
//                exit = fadeOut() + scaleOut(),
//                modifier = Modifier
//                    .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
//                    .padding(bottom = if (isLandscape) 0.dp else 100.dp, start = if (isLandscape) 80.dp else 0.dp)
//            ) {
//                ColorPickerPanel(
//                    currentColor = color,
//                    onColorSelected = {
//                        color = it
//                        expandedTool = null
//                        isErasing = false
//                    }
//                )
//            }
//
//            // Stroke Width Popup
//            AnimatedVisibility(
//                visible = expandedTool == "stroke" && canEdit,
//                enter = fadeIn() + scaleIn(),
//                exit = fadeOut() + scaleOut(),
//                modifier = Modifier
//                    .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
//                    .padding(bottom = if (isLandscape) 0.dp else 100.dp, start = if (isLandscape) 80.dp else 0.dp)
//            ) {
//                StrokeWidthPanel(
//                    currentStrokeWidth = strokeWidth,
//                    onStrokeWidthSelected = {
//                        strokeWidth = it
//                        expandedTool = null
//                    }
//                )
//            }
//        }
//    }
//}




