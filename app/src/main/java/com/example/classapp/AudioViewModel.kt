import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class AudioClientViewModel : ViewModel() {
    private val SAMPLE_RATE = 22050
    private val CHANNEL_CONFIG_RECORD = AudioFormat.CHANNEL_IN_MONO
    private val CHANNEL_CONFIG_PLAY = AudioFormat.CHANNEL_OUT_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE by lazy {
        AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG_RECORD,
            AUDIO_FORMAT
        )
    }

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var socket: Socket? = null
    private var isRecording = false

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Communicating : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun checkPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun connect(context: Context, host: String, port: Int = 8000) {
        if (!checkPermissions(context)) {
            _connectionState.value = ConnectionState.Error("Audio permission not granted")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket = Socket(host, port)
                _connectionState.value = ConnectionState.Connected
                Log.d("AudioClient", "Connected to $host:$port")
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
                Log.e("AudioClient", "Connection error", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startCommunication() {
        if (_connectionState.value !is ConnectionState.Connected) {
            _errorState.value = "Not connected to server"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Initialize audio recording
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG_RECORD,
                    AUDIO_FORMAT,
                    BUFFER_SIZE
                )

                // Initialize audio playback
                audioTrack = AudioTrack.Builder()
                    .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG_PLAY)
                        .build())
                    .setBufferSizeInBytes(BUFFER_SIZE)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                val inputStream = DataInputStream(socket?.getInputStream())
                val outputStream = DataOutputStream(socket?.getOutputStream())
                val buffer = ByteArray(BUFFER_SIZE)

                audioRecord?.startRecording()
                audioTrack?.play()
                isRecording = true
                _connectionState.value = ConnectionState.Communicating

                // Start receiving audio in a separate coroutine
                viewModelScope.launch(Dispatchers.IO) {
                    while (isRecording) {
                        try {
                            val bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)
                            if (bytesRead > 0) {
                                audioTrack?.write(buffer, 0, bytesRead)
                            }
                        } catch (e: Exception) {
                            break
                        }
                    }
                }

                // Send audio
                while (isRecording) {
                    val bytesRead = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: -1
                    if (bytesRead > 0) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Communication error: ${e.message}")
                _errorState.value = e.message
                stopCommunication()
            }
        }
    }

    fun stopCommunication() {
        isRecording = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null

                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null

                socket?.close()
                socket = null

                _connectionState.value = ConnectionState.Disconnected
            } catch (e: Exception) {
                _errorState.value = e.message
            }
        }
    }

    fun onPermissionGranted() {
        _connectionState.value = ConnectionState.Disconnected
    }

    fun onPermissionDenied() {
        _connectionState.value = ConnectionState.Error("Audio permission denied")
    }

    override fun onCleared() {
        super.onCleared()
        stopCommunication()
    }
}