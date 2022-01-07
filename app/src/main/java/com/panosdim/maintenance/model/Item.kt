package com.panosdim.maintenance.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate


@Parcelize
data class Item(val id: Int, val name: String, val periodicity: Number, val date: LocalDate) :
    Parcelable