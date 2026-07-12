package com.rakibcame.camera.commands

enum class CameraAction(val label: String) {
    TakePhoto("Take picture"), StartVideo("Start video"), StopVideo("Stop video"), PauseVideo("Pause video"), ResumeVideo("Resume video"),
    ZoomIn("Zoom in"), ZoomOut("Zoom out"), FrontCamera("Front camera"), BackCamera("Back camera"), FlashOn("Flash on"), FlashOff("Flash off"),
    NightMode("Night mode"), PortraitMode("Portrait mode"), OpenGallery("Open gallery"), SlowMotion("Slow motion"), HdrOn("HDR on"),
    IncreaseBrightness("Increase brightness"), ReduceBrightness("Reduce brightness"), CinematicVideo("Cinematic video"), RakibSelfie("rakib selfie")
}

data class VoiceCommand(
    val phrases: List<String>,
    val actions: List<CameraAction>,
    val description: String
)

object OfflineCommandRegistry {
    val builtInCommands = listOf(
        VoiceCommand(listOf("take picture", "capture", "ছবি তোল", "ছবি তুলুন"), listOf(CameraAction.TakePhoto), "Capture a still photo"),
        VoiceCommand(listOf("start video", "record video", "ভিডিও শুরু"), listOf(CameraAction.StartVideo), "Start video recording"),
        VoiceCommand(listOf("stop video", "ভিডিও বন্ধ"), listOf(CameraAction.StopVideo), "Stop video recording"),
        VoiceCommand(listOf("pause video"), listOf(CameraAction.PauseVideo), "Pause active recording"),
        VoiceCommand(listOf("resume video"), listOf(CameraAction.ResumeVideo), "Resume paused recording"),
        VoiceCommand(listOf("zoom in", "জুম ইন"), listOf(CameraAction.ZoomIn), "Increase zoom smoothly"),
        VoiceCommand(listOf("zoom out", "জুম আউট"), listOf(CameraAction.ZoomOut), "Decrease zoom smoothly"),
        VoiceCommand(listOf("front camera", "selfie camera"), listOf(CameraAction.FrontCamera), "Switch to front camera"),
        VoiceCommand(listOf("back camera"), listOf(CameraAction.BackCamera), "Switch to rear camera"),
        VoiceCommand(listOf("flash on"), listOf(CameraAction.FlashOn), "Enable torch/flash"),
        VoiceCommand(listOf("flash off"), listOf(CameraAction.FlashOff), "Disable torch/flash"),
        VoiceCommand(listOf("night mode"), listOf(CameraAction.NightMode), "Apply low-light scene profile"),
        VoiceCommand(listOf("portrait mode"), listOf(CameraAction.PortraitMode), "Apply portrait blur profile"),
        VoiceCommand(listOf("open gallery"), listOf(CameraAction.OpenGallery), "Open media gallery"),
        VoiceCommand(listOf("cinematic video"), listOf(CameraAction.CinematicVideo, CameraAction.HdrOn, CameraAction.StartVideo), "4K, stabilization, HDR, 60 FPS profile"),
        VoiceCommand(listOf("rakib selfie"), listOf(CameraAction.FrontCamera, CameraAction.PortraitMode, CameraAction.TakePhoto), "Custom sample: front camera, timer, beauty filter")
    )

    fun match(transcript: String): VoiceCommand? {
        val normalized = transcript.trim().lowercase()
        return builtInCommands.firstOrNull { command -> command.phrases.any { normalized.contains(it.lowercase()) } }
    }
}
