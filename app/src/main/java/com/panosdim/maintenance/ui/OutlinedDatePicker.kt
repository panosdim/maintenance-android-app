package com.panosdim.maintenance.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.panosdim.maintenance.R
import com.panosdim.maintenance.utils.toFormattedString
import com.panosdim.maintenance.utils.toLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedDatePicker(
    state: DatePickerState,
    label: String
) {
    val openDatePickerDialog = remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) {
        openDatePickerDialog.value = true
    }
    val confirmEnabled = remember {
        derivedStateOf { state.selectedDateMillis != null }
    }

    if (openDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                openDatePickerDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDatePickerDialog.value = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDatePickerDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        readOnly = true,
        interactionSource = source,
        value = state.selectedDateMillis?.toLocalDate()?.toFormattedString() ?: "",
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar Icon"
            )
        },
        onValueChange = { },
    )
}