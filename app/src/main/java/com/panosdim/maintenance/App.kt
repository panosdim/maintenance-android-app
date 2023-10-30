package com.panosdim.maintenance

import android.content.BroadcastReceiver
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

enum class MSG(val message: String) {
    ITEM("com.panosdim.maintenance.item")
}


val paddingLarge = 8.dp
val paddingExtraLarge = 16.dp
lateinit var onComplete: BroadcastReceiver


var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
const val CHANNEL_ID = "Maintenance-Channel"
const val TAG = "Maintenance-Tag"