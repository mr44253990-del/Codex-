package com.rakibcame.camera.ai

import com.rakibcame.camera.commands.OfflineCommandRegistry
import com.rakibcame.camera.commands.VoiceCommand

data class ObjectLabel(val name: String, val confidence: Float)
data class GestureSignal(val name: String, val actionHint: String)
data class SceneAdvice(val scene: String, val recommendation: String)

class OfflineVoiceEngine {
    var isListening: Boolean = false
        private set

    fun startContinuousListening() { isListening = true }
    fun stop() { isListening = false }
    fun detectCommand(transcript: String): VoiceCommand? = OfflineCommandRegistry.match(transcript)
}

class ObjectDetectionEngine {
    private val supportedLabels = setOf("Person", "Book", "Tree", "Phone", "Dog", "Car", "Flower", "Food", "QR", "Barcode", "Currency")
    fun labelsForPreviewFrame(): List<ObjectLabel> = supportedLabels.take(4).mapIndexed { index, label -> ObjectLabel(label, 0.90f - index * 0.07f) }
}

class GestureEngine {
    fun activeGestures(): List<GestureSignal> = listOf(
        GestureSignal("Palm show", "Capture"),
        GestureSignal("Two finger", "Zoom"),
        GestureSignal("Swipe left", "Gallery"),
        GestureSignal("Thumbs up", "Start recording")
    )
}

class SceneOptimizer {
    fun advise(labels: List<ObjectLabel>): SceneAdvice = when {
        labels.any { it.name == "Food" } -> SceneAdvice("Food", "Boost saturation and close focus")
        labels.any { it.name == "Person" } -> SceneAdvice("Portrait", "Enable face tracking and background blur")
        else -> SceneAdvice("Auto", "Keep stabilization, HDR, and AI guidance ready")
    }
}
