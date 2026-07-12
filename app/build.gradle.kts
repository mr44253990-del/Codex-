plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android { namespace = "com.rakibcame.camera"; compileSdk = 36
    defaultConfig { applicationId = "com.rakibcame.camera"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.1.0" }
}

kotlin { jvmToolchain(17) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("com.google.mediapipe:tasks-vision:0.10.31")
}
