package de.jrpie.android.launcher

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.jrpie.android.launcher.ui.EXTRA_CRASH_LOG
import de.jrpie.android.launcher.ui.ReportCrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.random.Random

private val NOTIFICATION_CHANNEL_CRASH = "launcher:crash"

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_CRASH,
            context.getString(R.string.notification_channel_crash),
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

fun requestNotificationPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }

    val permission =
        (activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    if (!permission) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf( android.Manifest.permission.POST_NOTIFICATIONS ),
            1
        )
    }
}

fun sendCrashNotification(context: Context, throwable: Throwable) {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    throwable.printStackTrace(printWriter)

    val intent = Intent(context, ReportCrashActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    intent.putExtra(EXTRA_CRASH_LOG, stringWriter.toString())

    val pendingIntent = PendingIntent.getActivity(
        context,
        Random.nextInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_CRASH)
        .setSmallIcon(R.drawable.baseline_bug_report_24)
        .setContentTitle(context.getString(R.string.notification_crash_title))
        .setContentText(context.getString(R.string.notification_crash_explanation))
        .setContentIntent(pendingIntent)
        .setAutoCancel(false)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notificationManager = NotificationManagerCompat.from(context)
    try {
        notificationManager.notify(
            0,
            builder.build()
        )
    } catch (e: SecurityException) {
        Log.e("Crash Notification", "Could not send notification")
    }
}
