package com.panosdim.maintenance.model

import java.time.LocalDate

data class Item(val id: Int, val name: String, val periodicity: Number, val date: LocalDate)