package com.rakib.offlinecall

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var meshService: OfflineMeshService? = null
    private var selectedPeerId: String? = "peer-0"
    private lateinit var root: LinearLayout
    private lateinit var peerList: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var radar: RadarView

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            meshService = (service as OfflineMeshService.LocalBinder).service()
            renderPeers()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            meshService = null
            statusText.text = "Service disconnected • reconnecting…"
            startMeshService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingScreen()
        handler.postDelayed({ requestNextPermission(0) }, 1200)
    }

    override fun onDestroy() {
        runCatching { unbindService(connection) }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode in permissionsToRequest.indices) {
            requestNextPermission(requestCode + 1)
        }
    }

    private fun showLoadingScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(36, 36, 36, 36)
            setBackgroundColor(Color.rgb(7, 11, 24))
        }
        layout.addView(text("H+", 72f, Color.rgb(0, 229, 255), true))
        layout.addView(text("Rakib offline call", 30f, Color.WHITE, true))
        layout.addView(text("Offline Bluetooth / nearby mesh voice, group PTT, messages, vibration alerts and smart reconnect.", 16f, Color.rgb(198, 210, 255)))
        layout.addView(text("No internet required after install. Real range depends on phone hardware, Bluetooth, nearby relays and Android permissions.", 14f, Color.rgb(255, 210, 140)))
        setContentView(layout)
    }

    private fun requestNextPermission(index: Int) {
        if (index >= permissionsToRequest.size) {
            startMeshService()
            showDashboard()
            return
        }
        val permission = permissionsToRequest[index]
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            requestNextPermission(index + 1)
        } else {
            requestPermissions(arrayOf(permission), index)
        }
    }

    private fun startMeshService() {
        val intent = Intent(this, OfflineMeshService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun showDashboard() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            setBackgroundColor(Color.rgb(7, 11, 24))
        }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)

        root.addView(text("Rakib offline call", 32f, Color.WHITE, true))
        root.addView(text("Owner: Rakib • Khulna • mr4425390@gmail.com", 15f, Color.rgb(173, 225, 255)))
        statusText = text("Scanning offline users…", 16f, Color.rgb(132, 255, 214), true)
        root.addView(statusText)
        root.addView(infoCard("Android 14+ permissions", "Mic, Bluetooth scan/advertise/connect, nearby devices, notification, vibration and foreground background service are requested one-by-one."))
        radar = RadarView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 360).apply { setMargins(0, 18, 0, 18) }
        }
        root.addView(radar)
        root.addView(actionRow())
        root.addView(text("Nearby users", 22f, Color.WHITE, true))
        peerList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(peerList)
        root.addView(groupPanel())
        handler.post(refreshTask)
    }

    private val refreshTask = object : Runnable {
        override fun run() {
            renderPeers()
            handler.postDelayed(this, 2500)
        }
    }

    private fun renderPeers() {
        if (!::peerList.isInitialized) return
        val peers = meshService?.snapshotPeers().orEmpty()
        statusText.text = "${peers.size.coerceAtLeast(1)} phones visible • background reconnect running • noise-gated PTT ready"
        radar.peers = peers.filter { it.id != "rakib-node" }
        peerList.removeAllViews()
        peers.filter { it.id != "rakib-node" }.forEach { peer ->
            peerList.addView(peerCard(peer))
        }
    }

    private fun actionRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(button("Scan / reconnect") {
                vibrate(35)
                startMeshService()
                renderPeers()
                toast("Smart reconnect started")
            })
            addView(button("Vibrate test") {
                vibrate(220)
                toast("Phone vibration alert sent locally")
            })
        }
    }

    private fun peerCard(peer: MeshPeer): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = rounded(Color.rgb(15, 24, 50))
            val margins = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 10, 0, 10) }
            layoutParams = margins
            addView(text("${peer.name}  •  ${peer.distanceMeters}m  •  ${peer.side}  •  ${peer.batteryPercent}%", 18f, Color.WHITE, true))
            addView(text("Direct mode: press Call to ring this phone, then talk only after accept. Hold PTT to avoid noise.", 13f, Color.rgb(198, 210, 255)))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(button("Select") {
                    selectedPeerId = peer.id
                    toast("Selected ${peer.name}")
                })
                addView(button("Call") {
                    val ok = meshService?.placeDirectCall(peer.id) == true
                    vibrate(if (ok) 120 else 30)
                    toast(if (ok) "Ringing ${peer.name}… waiting for accept" else "User unavailable")
                })
                addView(pttButton("Hold PTT") { meshService?.startPushToTalk(false, peer.id).orEmpty() })
            })
            addView(messageRow(peer.id, false))
        }
    }

    private fun groupPanel(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = rounded(Color.rgb(20, 42, 62))
            addView(text("Group channel", 22f, Color.WHITE, true))
            addView(text("Everyone in the group hears together. Hold mic button, release to stop, and mesh relay repeats through nearby phones when supported by hardware.", 14f, Color.rgb(198, 255, 226)))
            addView(pttButton("Hold group mic") { meshService?.startPushToTalk(true).orEmpty() })
            addView(messageRow(null, true))
        }
    }

    private fun messageRow(peerId: String?, group: Boolean): LinearLayout {
        val input = EditText(this).apply {
            hint = if (group) "Group message" else "Direct message"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(150, 162, 190))
            setSingleLine(true)
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(button("Send") {
                val sent = if (group) meshService?.sendGroupMessage(input.text.toString()) ?: 0 else if (peerId != null && meshService?.sendDirectMessage(peerId, input.text.toString()) == true) 1 else 0
                vibrate(if (sent > 0) 80 else 20)
                toast(if (sent > 0) "Message queued to $sent user(s)" else "Write a message or reconnect first")
                input.text.clear()
            })
        }
    }

    private fun pttButton(label: String, startMessage: () -> String): Button {
        return button(label) {}.apply {
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        vibrate(50)
                        text = "Talking…"
                        toast(startMessage())
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        text = label
                        toast(meshService?.stopPushToTalk().orEmpty())
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun button(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.rgb(7, 11, 24))
            setOnClickListener { onClick() }
        }
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
    }

    private fun infoCard(title: String, body: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = rounded(Color.rgb(15, 24, 50))
            addView(text(title, 18f, Color.WHITE, true))
            addView(text(body, 14f, Color.rgb(198, 210, 255)))
        }
    }

    private fun rounded(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = 24f
            setStroke(2, Color.rgb(0, 229, 255))
        }
    }

    private fun vibrate(milliseconds: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
        }
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun toast(message: String) {
        if (message.isNotBlank()) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val permissionsToRequest = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
        )
    }
}

class RadarView(context: Context) : View(context) {
    var peers: List<MeshPeer> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(0, 229, 255)
        repeat(3) { index -> canvas.drawCircle(centerX, centerY, (index + 1) * 52f, paint) }
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = Color.rgb(132, 255, 214)
        canvas.drawCircle(centerX, centerY, 14f, paint)
        paint.textSize = 24f
        canvas.drawText("You", centerX + 18f, centerY + 8f, paint)
        peers.forEachIndexed { index, peer ->
            val angle = when (peer.side) {
                "left" -> Math.PI
                "right" -> 0.0
                "behind" -> Math.PI / 2
                else -> -Math.PI / 2
            } + index * 0.23
            val radius = peer.distanceMeters.coerceIn(15, 160).toFloat()
            val x = centerX + kotlin.math.cos(angle).toFloat() * radius
            val y = centerY + kotlin.math.sin(angle).toFloat() * radius
            paint.color = Color.RED
            canvas.drawCircle(x, y, 11f, paint)
            paint.color = Color.WHITE
            paint.textSize = 20f
            canvas.drawText("${peer.distanceMeters}m ${peer.side}", x + 14f, y, paint)
        }
    }
}
