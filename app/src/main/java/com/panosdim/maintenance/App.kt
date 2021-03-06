package com.panosdim.maintenance

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

enum class MSG(val message: String) {
    ITEM("com.panosdim.maintenance.item")
}

var user = FirebaseAuth.getInstance().currentUser
val database = FirebaseDatabase.getInstance()
const val CHANNEL_ID = "Maintenance-Channel"
const val TAG = "Maintenance-Tag"