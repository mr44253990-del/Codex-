package com.rakibcame.camera

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rakibcame.camera.ai.GestureEngine
import com.rakibcame.camera.ai.ObjectDetectionEngine
import com.rakibcame.camera.ai.OfflineVoiceEngine
import com.rakibcame.camera.camera.CameraStatus
import com.rakibcame.camera.camera.ManualCameraState
import com.rakibcame.camera.ui.RakibcameCameraScreen

class MainActivity : ComponentActivity() {
    private val voiceEngine = OfflineVoiceEngine()
    private val objectDetectionEngine = ObjectDetectionEngine()
    private val gestureEngine = GestureEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUi()
        voiceEngine.startContinuousListening()
        gestureEngine.activeGestures()

        setContent {
            var lastCommand by remember { mutableStateOf("Say: take picture, zoom in, cinematic video") }
            RakibcameCameraScreen(
                manualState = ManualCameraState(),
                status = CameraStatus(micStatus = if (voiceEngine.isListening) "Listening" else "Muted"),
                labels = objectDetectionEngine.labelsForPreviewFrame(),
                lastCommand = lastCommand,
                onCapture = { lastCommand = "Captured photo via DSLR controls" },
                onVideo = { lastCommand = "Video recording profile armed" }
            )
        }
    }

    private fun hideSystemUi() {
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
