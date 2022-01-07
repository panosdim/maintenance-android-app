package com.panosdim.maintenance.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.maintenance.R
import com.panosdim.maintenance.fromEpochMilli
import com.panosdim.maintenance.toShowDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePicker(
    date: LocalDate?,
    onDateChange: (date: LocalDate) -> Unit
) {
    val activity = LocalContext.current as AppCompatActivity
    val picker = MaterialDatePicker.Builder.datePicker().build()
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
            .border(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(16.dp)
            .clickable {
                picker.show(activity.supportFragmentManager, picker.toString())
                picker.addOnPositiveButtonClickListener {
                    onDateChange(fromEpochMilli(it))
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = date?.toShowDateFormat(dateFormatter) ?: LocalDate.now()
                    .toShowDateFormat(dateFormatter),
                color = MaterialTheme.colors.onSurface,
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp, 24.dp),
                tint = MaterialTheme.colors.onSurface
            )
        }
    }
}