package com.panosdim.maintenance

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var manager: DownloadManager
    private lateinit var onComplete: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsViewModel by viewModels<ItemsViewModel>()

        // Handle new version installation after the download of APK file.
        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val referenceId = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (referenceId != -1L && referenceId == refId) {
                    val apkUri = manager.getUriForDownloadedFile(refId)
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    installIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(installIntent)
                }

            }
        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Check for permission to read/write to external storage
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    checkForNewVersion(this)
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(resources.getString(R.string.permission_title))
                        .setMessage(resources.getString(R.string.permission_description))
                        .setPositiveButton(resources.getString(R.string.dismiss)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                checkForNewVersion(this)
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

        FirebaseApp.initializeApp(this)
        createNotificationChannel(this)

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

        itemsRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Not used
                }
            })

        itemsRef.orderByChild("date").addChildEventListener(
            object : ChildEventListener {
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
}