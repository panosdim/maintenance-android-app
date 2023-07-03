package com.panosdim.maintenance.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.panosdim.maintenance.TAG
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

private fun getCalendarId(context: Context): Long? {
    val projection =
        arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

    var calCursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
        null,
        CalendarContract.Calendars._ID + " ASC"
    )

    if (calCursor != null && calCursor.count <= 0) {
        calCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )
    }

    if (calCursor != null) {
        if (calCursor.moveToFirst()) {
            val calName: String
            val calID: String
            val nameCol = calCursor.getColumnIndex(projection[1])
            val idCol = calCursor.getColumnIndex(projection[0])

            calName = calCursor.getString(nameCol)
            calID = calCursor.getString(idCol)

            Log.d(TAG, "Calendar name = $calName Calendar ID = $calID")

            calCursor.close()
            return calID.toLong()
        }
    }
    return null
}

fun insertEvent(context: Context, date: LocalDate, title: String): Long? {
    var calendarId: Long? = null
    var eventId: Long? = null

    if (allPermissionsGranted(context)) {
        calendarId = getCalendarId(context)
    }
    if (calendarId != null) {
        val startMillis: Long = Calendar.getInstance().run {
            set(date.year, date.monthValue - 1, date.dayOfMonth, 9, 0, 0)
            timeInMillis
        }
        val endMillis: Long = Calendar.getInstance().run {
            set(date.year, date.monthValue - 1, date.dayOfMonth, 10, 0, 0)
            timeInMillis
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(
                CalendarContract.Events.EVENT_TIMEZONE,
                TimeZone.getDefault().displayName
            )
        }
        val uri: Uri? = context.contentResolver.insert(
            CalendarContract.Events.CONTENT_URI,
            values
        )

        // get the event ID that is the last element in the Uri
        eventId = uri?.lastPathSegment?.toLong()
    }
    return eventId
}

fun updateEvent(context: Context, eventID: Long, date: LocalDate, title: String) {
    if (allPermissionsGranted(context)) {
        val startMillis: Long = Calendar.getInstance().run {
            set(date.year, date.monthValue - 1, date.dayOfMonth, 9, 0, 0)
            timeInMillis
        }
        val endMillis: Long = Calendar.getInstance().run {
            set(date.year, date.monthValue - 1, date.dayOfMonth, 10, 0, 0)
            timeInMillis
        }
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
        }
        val updateUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
        context.contentResolver.update(updateUri, values, null, null)
    }
}

fun deleteEvent(context: Context, eventID: Long) {
    if (allPermissionsGranted(context)) {
        val deleteUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
        context.contentResolver.delete(deleteUri, null, null)
    }
}