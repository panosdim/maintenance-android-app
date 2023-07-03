package com.panosdim.maintenance.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Item(
    var id: String? = null,
    var name: String = "",
    var periodicity: Int = 6,
    var date: String = "",
    var eventID: Long? = null
) :
    Parcelable