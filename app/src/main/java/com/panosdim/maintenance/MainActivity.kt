package com.panosdim.maintenance

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.toArgb
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.ui.MaintenanceItems
import com.panosdim.maintenance.ui.theme.MaintenanceTheme
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsViewModel by viewModels<ItemsViewModel>()

        FirebaseApp.initializeApp(this)
        createNotificationChannel()

        // Check for expired items
        val itemExpiredBuilder =
            PeriodicWorkRequestBuilder<ExpiredItemsWorker>(30, TimeUnit.DAYS)

        val itemExpiredWork = itemExpiredBuilder.build()
        // Then enqueue the recurring task:
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
            "itemExpired",
            ExistingPeriodicWorkPolicy.KEEP,
            itemExpiredWork
        )

        val itemsRef = database.getReference("items").child(user?.uid!!)

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Not used
            }
        })

        itemsRef.orderByChild("date").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
                // Not used
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Not used
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item?.id != null) {
                    item.id = dataSnapshot.key
                    itemsViewModel.updateItem(item)
                }

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item != null) {
                    item.id = dataSnapshot.key
                    itemsViewModel.removeItem(item)
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item != null) {
                    item.id = dataSnapshot.key
                    itemsViewModel.addItem(item)
                }
            }
        })

        setContent {
            MaintenanceTheme {
                window?.statusBarColor = MaterialTheme.colors.primaryVariant.toArgb()
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MaintenanceItems(itemsViewModel)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(R.string.channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}