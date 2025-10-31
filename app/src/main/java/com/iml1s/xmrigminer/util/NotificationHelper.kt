package com.iml1s.xmrigminer.util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.iml1s.xmrigminer.R
import com.iml1s.xmrigminer.service.MiningWorker

object NotificationHelper {

    private const val WARNING_NOTIFICATION_ID = 2001

    fun showWarning(context: Context, message: String) {
        val notification = NotificationCompat.Builder(context, MiningWorker.CHANNEL_ID)
            .setContentTitle("Mining Paused")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_mining)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WARNING_NOTIFICATION_ID, notification)
    }

    fun cancelWarning(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WARNING_NOTIFICATION_ID)
    }
}
