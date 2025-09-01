package com.example.classapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URISyntaxException
import java.util.UUID

class WhiteboardViewModel(val serverIp: String) : ViewModel() {
    private var socket: Socket? = null

    // StateFlows for UI state management
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _canEdit = MutableStateFlow(false)
    val canEdit: StateFlow<Boolean> = _canEdit

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus

    // PDF states
    private val _currentPdf = MutableStateFlow<ByteArray?>(null)
    val currentPdf: StateFlow<ByteArray?> = _currentPdf

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    // Image from current page/annotations
    private val _currentImage = MutableStateFlow<Bitmap?>(null)
    val currentImage: StateFlow<Bitmap?> = _currentImage

    // Track actual image dimensions (not just viewport)
    private val _imageSize = MutableStateFlow<Pair<Float, Float>?>(null)
    val imageSize: StateFlow<Pair<Float, Float>?> = _imageSize

    private var url: String = ""

    // Canvas dimensions
    private var viewWidth: Float = 0f
    private var viewHeight: Float = 0f

    // Server canvas dimensions
    private var serverCanvasWidth: Float = 800f  // Default values
    private var serverCanvasHeight: Float = 600f

    // Image position within canvas (for precise mapping)
    private var imageOffsetX: Float = 0f
    private var imageOffsetY: Float = 0f
    private var imageScaleFactor: Float = 1f

    companion object {
        private const val TAG = "WhiteboardViewModel"
    }

    init {
        this.url = serverIp
        connectSocket()
    }

    fun connectSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 50
                timeout = 20000
                reconnectionDelay = 5000
                forceNew = true
            }

            Log.d(TAG, "Attempting to connect to socket...")
            socket = IO.socket("http://${this.url}:5000", options)
            setupSocketListeners()
            socket?.connect()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Error creating socket", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during socket connection", e)
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d(TAG, "Socket connected successfully")
                viewModelScope.launch {
                    _isConnected.value = true
                }
                // Register viewport size after connection
                sendViewportSize()
            })

            on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener { args ->
                val error = args.firstOrNull()
                viewModelScope.launch {
                    _isConnected.value = false
                    Log.e(TAG, "Socket connection error: $error", Exception(error?.toString()))
                }
            })

            on(Socket.EVENT_DISCONNECT, Emitter.Listener {
                Log.d(TAG, "Socket disconnected")
                viewModelScope.launch {
                    _isConnected.value = false
                    _canEdit.value = false
                }
            })

            // Connection status updates from server
            on("connection_status", Emitter.Listener { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val status = data.optString("status", "")
                        val canEdit = data.optBoolean("can_edit", false)
                        val message = data.optString("message", "")

                        viewModelScope.launch {
                            _connectionStatus.value = message
                            _canEdit.value = canEdit
                            Log.d(TAG, "Connection status: $status, Can edit: $canEdit, Message: $message")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing connection status", e)
                }
            })

            // PDF handling
            on("new_pdf", Emitter.Listener { args ->
                Log.d(TAG, "Received new PDF event")
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val pdfData = data.getString("pdf_data")
                        val totalPages = data.optInt("total_pages", 0)
                        val currentPage = data.optInt("current_page", 0)

                        // Convert base64 to byte array
                        val pdfBytes = android.util.Base64.decode(pdfData, android.util.Base64.DEFAULT)

                        viewModelScope.launch {
                            _currentPdf.value = pdfBytes
                            _totalPages.value = totalPages
                            _currentPage.value = currentPage
                            Log.d(TAG, "PDF received: $totalPages pages, current page: $currentPage")

                            // Render the PDF page to bitmap
                            renderPdfPage(currentPage)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing PDF data", e)
                }
            })

            // Page change events
            on("change_page", Emitter.Listener { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val pageNumber = data.optInt("page_number", 0)

                        viewModelScope.launch {
                            _currentPage.value = pageNumber
                            Log.d(TAG, "Page changed to: $pageNumber")

                            // Render the new page
                            renderPdfPage(pageNumber)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing page change", e)
                }
            })

            // Handle clear annotations event
            on("clear_annotations", Emitter.Listener {
                viewModelScope.launch {
                    // You'll need to implement this based on your drawing mechanism
                    Log.d(TAG, "Received clear annotations command")
                }
            })

            // Legacy allow_student event
            on("allow_student", Emitter.Listener { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val data = args[0] as JSONObject
                    val allowedSid = data.optString("allowed_sid", "")

                    val mySid = socket?.id()
                    if (allowedSid == mySid) {
                        viewModelScope.launch {
                            _canEdit.value = true
                            Log.d(TAG, "I am allowed to draw!")
                        }
                    } else {
                        Log.d(TAG, "I am NOT allowed to draw.")
                    }
                }
            })

            // Handle coordinate updates from other clients
            on("coordinate_update", Emitter.Listener { args ->
                try {
                    // Process incoming coordinates from other clients if needed
                    Log.d(TAG, "Received coordinate update from another client")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing coordinate update", e)
                }
            })
        }
    }

    private suspend fun renderPdfPage(pageNumber: Int) {
        val pdfBytes = _currentPdf.value ?: return

        withContext(Dispatchers.IO) {
            try {
                // Create a temporary file to store the PDF
                val tempFile = File.createTempFile("temp_pdf_${UUID.randomUUID()}", ".pdf")
                val outputStream = FileOutputStream(tempFile)
                outputStream.write(pdfBytes)
                outputStream.close()

                // Set up PDF renderer
                val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)

                // Check if the page number is valid
                if (pageNumber >= 0 && pageNumber < renderer.pageCount) {
                    // Get the page and render it
                    val page = renderer.openPage(pageNumber)

                    // Create bitmap with appropriate resolution
                    val dpi = 300 // Higher DPI for better quality
                    val width = page.width * dpi / 72
                    val height = page.height * dpi / 72

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                    // Render the page to the bitmap
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Update the image size and bitmap
                    withContext(Dispatchers.Main) {
                        _imageSize.value = Pair(width.toFloat(), height.toFloat())
                        _currentImage.value = bitmap
                        calculateImagePlacement(width, height)
                    }

                    // Close resources
                    page.close()
                    renderer.close()
                    fileDescriptor.close()
                } else {
                    Log.e(TAG, "Invalid page number: $pageNumber for total pages: ${renderer.pageCount}")
                    renderer.close()
                    fileDescriptor.close()
                }

                // Delete the temporary file
                tempFile.delete()

            } catch (e: Exception) {
                Log.e(TAG, "Error rendering PDF page", e)
            }
        }
    }

    fun updateViewportSize(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
        sendViewportSize()

        // Recalculate image placement if we have an image
        _currentImage.value?.let {
            calculateImagePlacement(it.width, it.height)
        }
    }

    private fun calculateImagePlacement(imageWidth: Int, imageHeight: Int) {
        if (viewWidth <= 0 || viewHeight <= 0) return

        // Calculate aspect ratios
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
        val viewAspect = viewWidth / viewHeight

        // Calculate scale factor and offsets for proper mapping
        if (imageAspect > viewAspect) {
            // Image is wider than view (relative to height)
            imageScaleFactor = viewWidth / imageWidth.toFloat()
            imageOffsetX = 0f
            imageOffsetY = (viewHeight - (imageHeight * imageScaleFactor)) / 2f
        } else {
            // Image is taller than view (relative to width)
            imageScaleFactor = viewHeight / imageHeight.toFloat()
            imageOffsetX = (viewWidth - (imageWidth * imageScaleFactor)) / 2f
            imageOffsetY = 0f
        }

        Log.d(TAG, "Image placement: scale=$imageScaleFactor, offset=($imageOffsetX, $imageOffsetY)")
    }

    private fun sendViewportSize() {
        if (viewWidth <= 0 || viewHeight <= 0 || !_isConnected.value) return

        try {
            val data = JSONObject().apply {
                put("width", viewWidth)
                put("height", viewHeight)
            }
            socket?.emit("register_viewport", data)
            Log.d(TAG, "Sent viewport size: ${viewWidth}x$viewHeight")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending viewport size", e)
        }
    }

    fun requestEditPermission(question: String) {
        if (!_isConnected.value) return

        try {
            val data = JSONObject()
            data.put("question", question)
            socket?.emit("request_edit_permission", data)
            Log.d(TAG, "Edit permission requested with question: $question")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting edit permission", e)
        }
    }

    fun changePage(pageNumber: Int) {
        if (!_isConnected.value) return

        try {
            val data = JSONObject().apply {
                put("page_number", pageNumber)
            }
            socket?.emit("change_page", data)
            _currentPage.value = pageNumber
            Log.d(TAG, "Change page request sent: $pageNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error changing page", e)
        }
    }

    fun sendCoordinates(x: Float, y: Float, isStart: Boolean, lineWidth: Float, color: Int) {
        if (!_isConnected.value || !_canEdit.value) return

        try {
            // Convert screen coordinates to image-relative coordinates
            // First adjust for image position in the view
            val adjustedX = (x - imageOffsetX) / imageScaleFactor
            val adjustedY = (y - imageOffsetY) / imageScaleFactor

            // Then normalize to 0-1 range based on the actual image dimensions
            val imgSize = _imageSize.value ?: return
            val normalizedX = adjustedX / imgSize.first
            val normalizedY = adjustedY / imgSize.second

            // Don't send if outside the image boundaries
            if (normalizedX < 0 || normalizedX > 1 || normalizedY < 0 || normalizedY > 1) {
                Log.d(TAG, "Coordinate outside image boundaries, skipping: ($normalizedX, $normalizedY)")
                return
            }

            Log.d(TAG, "Mapped coordinates: ($x, $y) -> ($normalizedX, $normalizedY)")

            val data = JSONObject().apply {
                put("x", normalizedX)
                put("y", normalizedY)
                put("is_start", isStart)
                put("line_width", lineWidth)
                put("pen_color", String.format("#%06X", 0xFFFFFF and color))
                put("page_number", _currentPage.value)  // Include current page information
            }
            socket?.emit("send_coordinates", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending coordinates", e)
        }
    }

    override fun onCleared() {
        try {
            Log.d(TAG, "Disconnecting socket")
            socket?.disconnect()
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting socket", e)
        }
        super.onCleared()
    }
}