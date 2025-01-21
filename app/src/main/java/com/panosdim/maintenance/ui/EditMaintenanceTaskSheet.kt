package com.panosdim.maintenance.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.maintenance.R
import com.panosdim.maintenance.data.MainViewModel
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.paddingLarge
import com.panosdim.maintenance.utils.deleteEvent
import com.panosdim.maintenance.utils.getPeriodicity
import com.panosdim.maintenance.utils.insertEvent
import com.panosdim.maintenance.utils.toEpochMilli
import com.panosdim.maintenance.utils.toLocalDate
import com.panosdim.maintenance.utils.updateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.nextUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaintenanceTaskSheet(
    item: Item,
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val resources = context.resources
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var isLoading by remember {
        mutableStateOf(false)
    }

    val openDeleteDialog = remember { mutableStateOf(false) }

    // Sheet content
    if (bottomSheetState.isVisible) {
        var itemName by rememberSaveable { mutableStateOf(item.name) }
        var itemPeriodicity by rememberSaveable { mutableFloatStateOf(item.periodicity.toFloat()) }
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = item.date.toLocalDate().toEpochMilli()
            )
        var periodicityText by rememberSaveable {
            return@rememberSaveable mutableStateOf(getPeriodicity(itemPeriodicity, resources))
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = { Text(text = stringResource(id = R.string.delete_maintenance_task_dialog_title)) },
                text = {
                    Text(
                        stringResource(id = R.string.delete_maintenance_task_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                            isLoading = true

                            scope.launch {
                                viewModel.removeItem(item)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false

                                            if (it) {
                                                item.eventID?.let { eventID ->
                                                    deleteEvent(context, eventID)
                                                }

                                                Toast.makeText(
                                                    context,
                                                    R.string.delete_maintenance_task_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingLarge)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(paddingLarge)
            ) {
                Text(
                    stringResource(id = R.string.edit_maintenance_task),
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = itemName,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization =
                        KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = periodicityText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = itemPeriodicity,
                        onValueChange = {
                            itemPeriodicity = it.nextUp()
                            periodicityText = getPeriodicity(itemPeriodicity, resources)
                        },
                        valueRange = 6f..60f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                OutlinedDatePicker(
                    state = datePickerState,
                    label = stringResource(id = R.string.last_maintenance)
                )

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = { openDeleteDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.delete))
                    }

                    Button(
                        enabled = itemName.isNotBlank() && !isLoading,
                        onClick = {
                            isLoading = true

                            // Update leave object
                            item.name = itemName
                            item.periodicity = itemPeriodicity.toInt()
                            datePickerState.selectedDateMillis?.toLocalDate()?.let {
                                item.date = it.toString()
                            }

                            item.eventID?.let { eventId ->
                                datePickerState.selectedDateMillis?.toLocalDate()?.let { date ->
                                    val eventDate = date.plusMonths(itemPeriodicity.toLong())
                                    updateEvent(context, eventId, eventDate, itemName)
                                }
                            } ?: kotlin.run {
                                datePickerState.selectedDateMillis?.toLocalDate()?.let { date ->
                                    val eventDate = date.plusMonths(itemPeriodicity.toLong())
                                    item.eventID = insertEvent(context, eventDate, itemName)
                                }
                            }

                            scope.launch {
                                viewModel.updateItem(item)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (it) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.update_maintenance_task_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.update))
                    }
                }
            }
        }
    }
}