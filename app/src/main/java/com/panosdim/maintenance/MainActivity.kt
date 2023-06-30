package com.panosdim.maintenance

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.panosdim.maintenance.ui.MaintenanceItems
import com.panosdim.maintenance.ui.theme.MaintenanceTheme
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var manager: DownloadManager
    private lateinit var onComplete: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsViewModel by viewModels<ItemsViewModel>()
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


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

        FirebaseApp.initializeApp(this)
        createNotificationChannel(this)

        // Check for new version
        checkForNewVersion(this)

        // Check for Notifications Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

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

        setContent {
            val items =
                itemsViewModel.maintenanceItems.collectAsStateWithLifecycle(initialValue = emptyList())
            MaintenanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MaintenanceItems(items.value)
                }
            }
        }
    }
}