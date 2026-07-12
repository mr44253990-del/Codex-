package com.rakibcame.camera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rakibcame.camera.ai.ObjectLabel
import com.rakibcame.camera.camera.CameraStatus
import com.rakibcame.camera.camera.ManualCameraState

@Composable
fun RakibcameCameraScreen(
    manualState: ManualCameraState,
    status: CameraStatus,
    labels: List<ObjectLabel>,
    lastCommand: String,
    onCapture: () -> Unit,
    onVideo: () -> Unit
) {
    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            CameraPreviewPlaceholder(labels, Modifier.fillMaxSize())
            TopStatusBar(status, manualState, Modifier.align(Alignment.TopCenter))
            DslrWheel(manualState, Modifier.align(Alignment.CenterStart).padding(16.dp))
            QuickButtons(onCapture, onVideo, Modifier.align(Alignment.CenterEnd).padding(16.dp))
            VoiceAssistantStrip(lastCommand, Modifier.align(Alignment.BottomCenter).padding(12.dp))
        }
    }
}

@Composable
private fun CameraPreviewPlaceholder(labels: List<ObjectLabel>, modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFF101820)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Live Camera Preview", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text("CameraX surface + TFLite labels", color = Color(0xFF9EEBFF))
            labels.forEach { Text("${it.name} ${(it.confidence * 100).toInt()}%", color = Color(0xFFFFD166)) }
        }
    }
}

@Composable
private fun TopStatusBar(status: CameraStatus, manualState: ManualCameraState, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = Color(0xAA000000)) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Battery ${status.battery}", color = Color.White)
            Text("Storage ${status.storage}", color = Color.White)
            Text("${manualState.fps} FPS", color = Color.White)
            Text(manualState.resolution, color = Color.White)
            Text(status.aiStatus, color = Color.Cyan)
            Text(status.micStatus, color = Color.Green)
            Text("HDR ${if (manualState.hdrEnabled) "ON" else "OFF"}", color = Color.White)
        }
    }
}

@Composable
private fun DslrWheel(state: ManualCameraState, modifier: Modifier = Modifier) {
    Column(modifier.background(Color(0x99000000), RoundedCornerShape(18.dp)).padding(12.dp)) {
        Text("ISO ${state.iso}", color = Color.White)
        Text("Shutter ${state.shutterSpeed}", color = Color.White)
        Text("WB ${state.whiteBalance}", color = Color.White)
        Text("EV ${state.exposureCompensation}", color = Color.White)
        Text(state.focusMode, color = Color.White)
        Text("Zoom ${state.zoomRatio}x", color = Color.White)
    }
}

@Composable
private fun QuickButtons(onCapture: () -> Unit, onVideo: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onCapture) { Text("Capture") }
        Button(onClick = onVideo) { Text("Video") }
        Button(onClick = {}) { Text("Gallery") }
        Button(onClick = {}) { Text("AI") }
        Button(onClick = {}) { Text("Flash") }
        Button(onClick = {}) { Text("Lens") }
    }
}

@Composable
private fun VoiceAssistantStrip(lastCommand: String, modifier: Modifier = Modifier) {
    Row(modifier.background(Color(0xCC0B132B), RoundedCornerShape(20.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("🎙 Voice assistant active", color = Color.White)
        Spacer(Modifier.width(16.dp))
        Text(lastCommand, color = Color(0xFF80FFDB))
    }
}
