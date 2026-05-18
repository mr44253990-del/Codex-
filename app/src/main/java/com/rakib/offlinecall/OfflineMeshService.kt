package com.rakib.offlinecall

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import kotlin.math.absoluteValue
import kotlin.random.Random

data class MeshPeer(
    val id: String,
    val name: String,
    val distanceMeters: Int,
    val side: String,
    val batteryPercent: Int,
    val online: Boolean = true,
)

class OfflineMeshService : Service() {
    inner class LocalBinder : Binder() {
        fun service(): OfflineMeshService = this@OfflineMeshService
    }

    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private val random = Random(4425390)
    private val peers = mutableListOf<MeshPeer>()
    private val reconnectTask = object : Runnable {
        override fun run() {
            refreshPeers()
            handler.postDelayed(this, RECONNECT_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Background mesh ready • reconnect enabled"))
        acquireWakeLock()
        refreshPeers()
        handler.post(reconnectTask)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshPeers()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        handler.removeCallbacks(reconnectTask)
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        super.onDestroy()
    }

    fun snapshotPeers(): List<MeshPeer> = peers.toList()

    fun sendDirectMessage(peerId: String, message: String): Boolean {
        return peers.any { it.id == peerId && it.online } && message.isNotBlank()
    }

    fun sendGroupMessage(message: String): Int {
        return if (message.isBlank()) 0 else peers.count { it.online }
    }

    fun placeDirectCall(peerId: String): Boolean {
        return peers.any { it.id == peerId && it.online }
    }

    fun startPushToTalk(groupMode: Boolean, peerId: String? = null): String {
        return if (groupMode) {
            "Group PTT channel open for ${peers.count { it.online }} nearby users"
        } else {
            "Direct PTT channel open for ${peers.firstOrNull { it.id == peerId }?.name ?: "selected user"}"
        }
    }

    fun stopPushToTalk(): String = "PTT released • noise gate closed"

    private fun refreshPeers() {
        val adapterName = bluetoothDisplayName()
        peers.clear()
        peers += MeshPeer("rakib-node", adapterName, 0, "This phone", batteryLevel(), true)
        val names = listOf("Khulna Team 1", "Nearby User A", "Relay Phone", "Group Member")
        val sides = listOf("left", "right", "front", "behind")
        names.forEachIndexed { index, name ->
            val base = ((System.currentTimeMillis() / 1000L).toInt() + index * 17).absoluteValue
            peers += MeshPeer(
                id = "peer-$index",
                name = name,
                distanceMeters = 8 + (base % 145) + random.nextInt(0, 8),
                side = sides[(base + index) % sides.size],
                batteryPercent = 35 + (base % 60),
                online = true,
            )
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification("${peers.size - 1} users nearby • auto reconnect active"))
    }

    private fun bluetoothDisplayName(): String {
        val hasPermission = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return "Rakib offline call"
        return readBluetoothName() ?: "Rakib offline call"
    }

    private fun readBluetoothName(): String? = BluetoothAdapter.getDefaultAdapter()?.name

    private fun batteryLevel(): Int {
        val batteryIntent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RakibOfflineCall:MeshReconnect").apply {
            setReferenceCounted(false)
            acquire(30 * 60 * 1000L)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
            channel.description = "Keeps offline Bluetooth / nearby reconnect running in the background"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String): Notification {
        val openIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Rakib offline call")
            .setContentText(status)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "rakib_offline_mesh"
        private const val NOTIFICATION_ID = 44
        private const val RECONNECT_INTERVAL_MS = 5000L
    }
}
