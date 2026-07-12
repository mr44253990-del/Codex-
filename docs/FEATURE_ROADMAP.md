# rakibcame — Advanced Offline AI DSLR Camera Assistant

rakibcame is designed as a landscape-first, fully offline, AI-assisted DSLR-style Android camera application. The first code milestone in this repository establishes the Android project, the fullscreen Compose camera cockpit, and modular offline AI service boundaries.

## Implemented foundation

- **Landscape fullscreen startup:** `MainActivity` locks the launcher activity to landscape through the manifest and hides system bars at runtime.
- **Camera cockpit UI:** Compose renders a DSLR-style preview surface, top telemetry bar, left manual-control wheel, right quick controls, live object labels, and persistent voice-assistant status.
- **Offline voice command registry:** Built-in Bangla/English phrases map to single or multi-action camera workflows, including `cinematic video` and `rakib selfie`.
- **AI engine seams:** Dedicated Kotlin classes define replaceable modules for voice recognition, object detection labels, gesture signals, and scene optimization.
- **Manual camera model:** ISO, shutter speed, white balance, exposure, focus, zoom, HDR, stabilization, resolution, and FPS are represented in immutable state models.

## Next implementation milestones

1. Replace the preview placeholder with CameraX `PreviewView` and bind photo/video use cases.
2. Add runtime permission flow for camera, microphone, and media access.
3. Integrate Vosk or Whisper Tiny for offline continuous speech recognition.
4. Load TensorFlow Lite object detection and MediaPipe gesture models from app assets.
5. Persist custom multi-action voice commands with Room.
6. Implement gallery indexing, secure albums, and smart search metadata.
7. Add pro video profiles for 4K/60 FPS, slow motion, timelapse, HDR, and stabilization where device capabilities allow.

## Device capability notes

Features such as 8K video, optical zoom, ultra-wide lens switching, multi-camera fusion, and high-FPS capture depend on Android device hardware and Camera2 capability levels. The app should expose only supported controls after querying camera characteristics.
