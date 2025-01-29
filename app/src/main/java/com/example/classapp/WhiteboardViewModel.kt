package com.example.classapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

class WhiteboardViewModel : ViewModel() {
    var socket: Socket? = null

    // StateFlows for UI state management
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _currentImage = MutableStateFlow<Bitmap?>(null)
    val currentImage: StateFlow<Bitmap?> = _currentImage

    // Canvas dimensions
    private var viewWidth: Float = 0f
    private var viewHeight: Float = 0f

    // Server canvas dimensions
    private var serverCanvasWidth: Float = 0f
    private var serverCanvasHeight: Float = 0f

    companion object {
        private const val TAG = "WhiteboardViewModel"
    }

    init {
        connectSocket()
    }

    private fun connectSocket() {
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
            socket = IO.socket("http://192.168.1.8:5000", options)

            setupSocketListeners()
            socket?.connect()
            Log.d(TAG, "Socket connect() called")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Error creating socket", e)
        } catch (e: Exception) {
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during socket connection", e)
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected successfully")
                viewModelScope.launch {
                    _isConnected.value = true
                }
                // Register viewport size after connection
                sendViewportSize()
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                viewModelScope.launch {
                    _isConnected.value = false
                    Log.e(TAG, "Socket connection error: $error", Exception(error?.toString()))
                }
            }

            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                viewModelScope.launch {
                    _isConnected.value = false
                }
            }

            // Image handling
            on("new_image") { args ->
//                Log.d(TAG, "Received new image event")
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val imageData = data.getString("image_data")

                        // Update server canvas dimensions
                        serverCanvasWidth = data.optDouble("canvas_width", serverCanvasWidth.toDouble()).toFloat()
                        serverCanvasHeight = data.optDouble("canvas_height", serverCanvasHeight.toDouble()).toFloat()

                        // Convert base64 to Bitmap
                        val imageBytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        viewModelScope.launch {
                            _currentImage.value = bitmap
                        }
//                        Log.d(TAG, "Successfully processed image data")
                    } else {
                        Log.e(TAG, "Invalid image data format: ${args.joinToString()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image data", e)
                }
            }
        }
    }

    fun updateViewportSize(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
        sendViewportSize()
    }

    private fun sendViewportSize() {
        if (viewWidth <= 0 || viewHeight <= 0) return

        try {
            val data = JSONObject().apply {
                put("width", viewWidth)
                put("height", viewHeight)
            }
            socket?.emit("register_viewport", data)
//            Log.d(TAG, "Sent viewport size: ${viewWidth}x$viewHeight")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending viewport size", e)
        }
    }

    fun sendCoordinates(x: Float, y: Float, isStart: Boolean, lineWidth: Float, color: Int) {
        try {
            // Normalize coordinates to viewport size
            val normalizedX = x / viewWidth
            val normalizedY = y / viewHeight

            val data = JSONObject().apply {
                put("x", normalizedX)
                put("y", normalizedY)
                put("is_start", isStart)
                put("line_width", lineWidth)
                put("pen_color", String.format("#%06X", 0xFFFFFF and color))
            }
//            Log.d(TAG, "Sending coordinates: $data")
            socket?.emit("send_coordinates", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending coordinates", e)
        }
    }

    fun clearImage() {
        viewModelScope.launch {
            _currentImage.value = null
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