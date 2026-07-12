package com.rakibcame.camera.camera

data class ManualCameraState(
    val iso: Int = 100,
    val shutterSpeed: String = "1/60",
    val whiteBalance: String = "Auto",
    val exposureCompensation: Float = 0f,
    val focusMode: String = "Auto Focus",
    val zoomRatio: Float = 1f,
    val hdrEnabled: Boolean = true,
    val stabilizationEnabled: Boolean = true,
    val resolution: String = "4K",
    val fps: Int = 60
)

data class CameraStatus(
    val battery: String = "--%",
    val storage: String = "Ready",
    val aiStatus: String = "AI Offline",
    val micStatus: String = "Listening",
    val flashEnabled: Boolean = false
)
