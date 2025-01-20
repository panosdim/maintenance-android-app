package com.panosdim.maintenance.utils

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import com.panosdim.maintenance.CHANNEL_ID
import com.panosdim.maintenance.R
import com.panosdim.maintenance.TAG
import com.panosdim.maintenance.model.FileMetadata
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import javax.net.ssl.HttpsURLConnection

var refId: Long = -1

private val json = Json { ignoreUnknownKeys = true }

fun checkForNewVersion(context: Context) {
    val metadataFileName = "output-metadata.json"
    val apkFileName = "app-release.apk"
    val backendUrl = "https://apps.dsw.mywire.org/maintenance/"
    val url: URL

    try {
        url = URL(backendUrl + metadataFileName)
        val conn = url.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = true
        conn.requestMethod = "GET"
        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.useCaches = false

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val data = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val fileMetadata = json.decodeFromString<FileMetadata>(data)
            val version = fileMetadata.elements[0].versionCode

            val appVersion = PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )
            )

            if (version > appVersion) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.new_version),
                        Toast.LENGTH_LONG
                    ).show()
                }

                val versionName = fileMetadata.elements[0].versionName

                // Download APK file
                val apkUri = Uri.parse(backendUrl + apkFileName)
                downloadNewVersion(context, apkUri, versionName)
            }
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
}

private fun downloadNewVersion(context: Context, downloadUrl: Uri, version: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
        DownloadManager.Request(downloadUrl)
    request.setDescription("Downloading new version of Maintenance.")
    request.setTitle("New Maintenance Version: $version")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "Maintenance-${version}.apk"
    )
    refId = manager.enqueue(request)
}

fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    val name = context.getString(R.string.channel_name)
    val importance = NotificationManager.IMPORTANCE_DEFAULT

    val channel = NotificationChannel(CHANNEL_ID, name, importance)
    // Register the channel with the system
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun convertMonthsToYearsAndMonths(months: Int): Pair<Int, Int> {
    val years = months / 12
    val remainingMonths = months % 12
    return Pair(years, remainingMonths)
}

fun getPeriodicity(periodicity: Float, resources: Resources): String {
    val itemPeriodicity = convertMonthsToYearsAndMonths(periodicity.toInt())
    return when (itemPeriodicity.first) {
        0 -> resources.getString(
            R.string.months_periodicity,
            itemPeriodicity.second
        )

        else -> {
            when (itemPeriodicity.second) {
                0 -> resources.getQuantityString(
                    R.plurals.periodicity,
                    itemPeriodicity.first,
                    itemPeriodicity.first
                )

                else -> resources.getQuantityString(
                    R.plurals.periodicity_years_and_months,
                    itemPeriodicity.first,
                    itemPeriodicity.first,
                    itemPeriodicity.second
                )
            }
        }
    }
}

fun formatDuration(duration: Duration, resources: Resources): String {
    val years = duration.toDays() / 365
    val months = (duration.toDays() % 365) / 30
    val days = (duration.toDays() % 365) % 30

    val sb = StringBuilder()
    if (years > 0) sb.append(
        resources.getQuantityString(
            R.plurals.years,
            years.toInt(),
            years.toInt()
        )
    ).append(" ")
    if (months > 0) sb.append(
        resources.getQuantityString(
            R.plurals.months,
            months.toInt(),
            months.toInt()
        )
    ).append(" ")
    if (days > 0) sb.append(
        resources.getQuantityString(
            R.plurals.days,
            days.toInt(),
            days.toInt()
        )
    ).append(" ")

    return sb.toString().trim()
}

fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(
        context, it
    ) == PackageManager.PERMISSION_GRANTED
}

const val REQUEST_CODE_PERMISSIONS = 10
val REQUIRED_PERMISSIONS =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mutableListOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.POST_NOTIFICATIONS
        ).toTypedArray()
    } else {
        mutableListOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
        ).toTypedArray()
    }