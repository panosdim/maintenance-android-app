package com.panosdim.maintenance.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    var id: String? = null,
    var name: String = "",
    var periodicity: Int = 6,
    var date: String = "",
    var eventID: Long? = null
)