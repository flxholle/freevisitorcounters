package com.asdoi.freevisitorcounters

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager

object Notification {
    private const val CHANNEL_ID = "notificationid"
    private const val NOTIFICATION_ID = 30

    fun sendNotification(context: Context) {
        Thread {
            val visitorCounter = Parser.parseWebsite(getLastURL(context))
            if (visitorCounter != null) {
                if (visitorCounter.visitorsOverview.today > 0) {
                    // Create an explicit intent for an Activity in your app
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent =
                        PendingIntent.getActivity(context, 0, intent, 0)


                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_supervisor_account_24)
                        .setContentTitle(context.getString(R.string.new_visitors_today))
                        .setContentText(
                            context.getString(
                                R.string.new_visitors_msg,
                                visitorCounter.visitorsOverview.today.toString(),
                                visitorCounter.visitorsOverview.all.toString()
                            )
                        )
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Create the NotificationChannel
                        val name = context.getString(R.string.new_visitors_today)
                        val channel = NotificationChannel(
                            CHANNEL_ID,
                            name,
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        channel.description = context.getString(R.string.new_visitors_today)
                        // Register the channel with the system; you can't change the importance
                        // or other notification behaviors after this
                        val notificationManager =
                            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    with(NotificationManagerCompat.from(context)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(NOTIFICATION_ID, builder.build())
                    }
                }
            }
        }.start()
    }

    private fun getLastURL(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("last_url", "")!!
    }
}