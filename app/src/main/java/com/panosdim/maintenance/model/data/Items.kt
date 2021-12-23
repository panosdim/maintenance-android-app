package com.panosdim.maintenance.model.data

import com.panosdim.maintenance.model.Item
import java.time.LocalDate


val items = listOf(
    Item(
        1,
        "Ηλιακός",
        2,
        LocalDate.of(2020, 9, 3),
    ),
    Item(
        2,
        "Καλοριφέρ",
        1,
        LocalDate.of(2020, 7, 10),
    ),
    Item(
        3,
        "Βάψιμο Κάγκελα",
        4,
        LocalDate.of(2015, 5, 19)
    ),
    Item(
        4,
        "Μηχανισμός Πόρτας",
        1,
        LocalDate.of(2020, 8, 30)
    ),
    Item(
        5,
        "Λάδια Viking",
        1,
        LocalDate.of(2021, 4, 22)
    ),
    Item(
        6,
        "Πυροσβεστήρας",
        2,
        LocalDate.of(2017, 2, 10)
    )
)
