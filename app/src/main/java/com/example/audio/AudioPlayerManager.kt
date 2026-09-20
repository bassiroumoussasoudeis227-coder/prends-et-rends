package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class AudioPlaybackState {
    IDLE,
    PLAYING,
    PAUSED
}

interface AudioPlayerManager {
    val playbackState: StateFlow<AudioPlaybackState>
    val currentPlayingPath: StateFlow<String?>
    fun play(filePath: String)
    fun pause()
    fun stop()
    fun release()
}

interface AudioRecorderManager {
    val isRecording: StateFlow<Boolean>
    fun startRecording(outputFile: File): Boolean
    fun stopRecording(): String?
    fun cancelRecording()
}

class AndroidAudioPlayerManager(private val context: Context) : AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(AudioPlaybackState.IDLE)
    override val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private val _currentPlayingPath = MutableStateFlow<String?>(null)
    override val currentPlayingPath: StateFlow<String?> = _currentPlayingPath.asStateFlow()

    override fun play(filePath: String) {
        val file = File(filePath)
        if (!file.exists() && !filePath.startsWith("content://") && !filePath.startsWith("http")) {
            Log.e("AudioPlayer", "File does not exist: $filePath")
            return
        }

        if (_currentPlayingPath.value == filePath && _playbackState.value == AudioPlaybackState.PAUSED) {
            mediaPlayer?.start()
            _playbackState.value = AudioPlaybackState.PLAYING
            return
        }

        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _playbackState.value = AudioPlaybackState.IDLE
                    _currentPlayingPath.value = null
                }
                start()
            }
            _currentPlayingPath.value = filePath
            _playbackState.value = AudioPlaybackState.PLAYING
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio from $filePath", e)
            _playbackState.value = AudioPlaybackState.IDLE
            _currentPlayingPath.value = null
        }
    }

    override fun pause() {
        if (_playbackState.value == AudioPlaybackState.PLAYING) {
            mediaPlayer?.pause()
            _playbackState.value = AudioPlaybackState.PAUSED
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Exception stopping player", e)
        } finally {
            mediaPlayer = null
            _playbackState.value = AudioPlaybackState.IDLE
            _currentPlayingPath.value = null
        }
    }

    override fun release() {
        stop()
    }
}

class AndroidAudioRecorderManager(private val context: Context) : AudioRecorderManager {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    override fun startRecording(outputFile: File): Boolean {
        return try {
            cancelRecording()
            currentFile = outputFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            _isRecording.value = false
            currentFile = null
            false
        }
    }

    override fun stopRecording(): String? {
        if (!_isRecording.value) return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            currentFile?.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to stop recording", e)
            mediaRecorder = null
            _isRecording.value = false
            null
        }
    }

    override fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            currentFile?.delete()
            currentFile = null
        }
    }
}
