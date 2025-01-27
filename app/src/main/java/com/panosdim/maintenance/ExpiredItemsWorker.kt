package com.panosdim.maintenance

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.utils.toLocalDate
import java.time.LocalDate.now
import java.time.temporal.ChronoUnit


class ExpiredItemsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        val mBuilderPassed = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_passed_title))
            .setContentText(applicationContext.getString(R.string.notification_passed_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val mBuilderApproaching = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_approaching_title))
            .setContentText(applicationContext.getString(R.string.notification_approaching_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val itemsRef = database.getReference("items").child(user?.uid!!)

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Not used
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val today = now()
                var maintenancePassedNotification = false
                var maintenanceApproachingNotification = false
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    if (item != null) {
                        val maintenanceDate = item.date.toLocalDate()
                            .plusMonths(item.periodicity.toLong())
                        if (!maintenancePassedNotification && maintenanceDate.isBefore(today)
                        ) {
                            with(NotificationManagerCompat.from(applicationContext)) {
                                if (ActivityCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    notify(0, mBuilderPassed.build())
                                    maintenancePassedNotification = true
                                }
                            }
                        }
                        if (!maintenanceApproachingNotification) {
                            val daysUntilItemDate = ChronoUnit.DAYS.between(today, maintenanceDate)
                            if (daysUntilItemDate in 0..15) {
                                with(NotificationManagerCompat.from(applicationContext)) {
                                    if (ActivityCompat.checkSelfPermission(
                                            applicationContext,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notify(1, mBuilderApproaching.build())
                                        maintenanceApproachingNotification = true
                                    }
                                }
                            }
                        }
                        if (maintenancePassedNotification && maintenanceApproachingNotification) {
                            break
                        }
                    }
                }
            }
        })

        // Indicate success or failure with your return value:
        return Result.success()
    }

}
