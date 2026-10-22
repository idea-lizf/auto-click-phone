package com.autoclick.app.keepalive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.autoclick.app.AutoClickApp
import com.autoclick.app.MainActivity
import com.autoclick.app.R
import com.autoclick.app.engine.RunEvent
import com.autoclick.app.engine.Script
import com.autoclick.app.engine.ScriptEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EngineForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var engineJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        val notification = buildNotification("准备就绪", "点击运行脚本")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val scriptJson = intent.getStringExtra(EXTRA_SCRIPT_JSON) ?: return START_NOT_STICKY
                val scriptId = intent.getLongExtra(EXTRA_SCRIPT_ID, 0L)
                val script = com.autoclick.app.engine.ScriptParser.parse(scriptJson)
                startEngine(script, scriptId)
            }
            ACTION_STOP -> {
                stopEngine()
            }
        }
        return START_NOT_STICKY
    }

    private fun startEngine(script: Script, scriptId: Long) {
        stopEngine()
        val app = application as AutoClickApp
        val engine = ScriptEngine(
            gateway = app.gateway,
            runLogRepository = app.runLogRepository
        )
        engineJob = scope.launch {
            engine.run(script, scriptId).collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun stopEngine() {
        engineJob?.cancel()
        engineJob = null
        updateNotification("已停止", "点击重新运行")
    }

    private fun handleEvent(event: RunEvent) {
        when (event) {
            is RunEvent.RunStarted -> updateNotification(
                "运行中: ${event.scriptName}",
                "共 ${event.totalSteps} 步, 重复 ${event.repeat} 次"
            )
            is RunEvent.StepStarted -> updateNotification(
                "运行中",
                "步骤 ${event.stepIndex + 1}: ${event.action} ${event.detail}"
            )
            is RunEvent.StepSuccess -> updateNotification(
                "运行中",
                "步骤 ${event.stepIndex + 1}: ${event.action} 完成"
            )
            is RunEvent.StepFailed -> {
                if (event.willRetry) {
                    updateNotification("重试中", "步骤 ${event.stepIndex + 1}: ${event.message}")
                } else {
                    updateNotification("运行中", "步骤 ${event.stepIndex + 1} 失败: ${event.message}")
                }
            }
            is RunEvent.RunCompleted -> {
                val title = if (event.success) "运行完成" else "运行失败"
                updateNotification(title, event.message ?: "")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_name)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(title, content))
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        wakeLock?.acquire(WAKE_LOCK_TIMEOUT_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        engineJob?.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
        scope.cancel()
    }

    companion object {
        const val ACTION_START = "com.autoclick.app.action.START"
        const val ACTION_STOP = "com.autoclick.app.action.STOP"
        const val EXTRA_SCRIPT_JSON = "script_json"
        const val EXTRA_SCRIPT_ID = "script_id"

        private const val CHANNEL_ID = "engine_foreground"
        private const val NOTIFICATION_ID = 1
        private const val WAKE_LOCK_TAG = "AutoClickPhone:Engine"
        private const val WAKE_LOCK_TIMEOUT_MS = 30 * 60 * 1000L

        fun startScript(context: Context, scriptJson: String, scriptId: Long) {
            val intent = Intent(context, EngineForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SCRIPT_JSON, scriptJson)
                putExtra(EXTRA_SCRIPT_ID, scriptId)
            }
            context.startForegroundService(intent)
        }

        fun stopScript(context: Context) {
            val intent = Intent(context, EngineForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startForegroundService(intent)
        }
    }
}
