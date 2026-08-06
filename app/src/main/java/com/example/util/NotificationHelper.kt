package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "dprd_bitung_wfh_channel"
    private const val CHANNEL_NAME = "Pengingat Absensi WFH DPRD Bitung"
    private const val NOTIF_ID_MASUK = 1001
    private const val NOTIF_ID_PULANG = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat absensi masuk (07.00-09.00) dan pulang (12.00-14.00) Sekretariat DPRD Bitung"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showAbsensiReminder(context: Context, isMasuk: Boolean) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isMasuk) NOTIF_ID_MASUK else NOTIF_ID_PULANG,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isMasuk) {
            "⏰ Pengingat Absensi Masuk WFH"
        } else {
            "⏰ Pengingat Absensi Pulang WFH"
        }

        val message = if (isMasuk) {
            "Halo PNS/PPPK DPRD Kota Bitung, jam absensi MASUK (07.00 - 09.00 WITA) sedang berlangsung. Segera lakukan verifikasi wajah & GPS!"
        } else {
            "Jam absensi PULANG (12.00 - 14.00 WITA) telah dibuka. Jangan lupa lakukan absensi sebelum jam 14.00!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(if (isMasuk) NOTIF_ID_MASUK else NOTIF_ID_PULANG, builder.build())
    }
}
