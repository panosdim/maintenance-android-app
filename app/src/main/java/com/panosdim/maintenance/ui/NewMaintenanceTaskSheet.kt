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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.maintenance.R
import com.panosdim.maintenance.data.MainViewModel
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.paddingLarge
import com.panosdim.maintenance.utils.getPeriodicity
import com.panosdim.maintenance.utils.insertEvent
import com.panosdim.maintenance.utils.toEpochMilli
import com.panosdim.maintenance.utils.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.nextUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMaintenanceTaskSheet(
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val resources = context.resources

    var isLoading by remember {
        mutableStateOf(false)
    }

    // Sheet content
    if (bottomSheetState.isVisible) {
        var itemName by rememberSaveable { mutableStateOf("") }
        var itemPeriodicity by rememberSaveable { mutableFloatStateOf(12f) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = LocalDate.now().toEpochMilli())
        var periodicityText by rememberSaveable {
            return@rememberSaveable mutableStateOf(getPeriodicity(itemPeriodicity, resources))
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
                    stringResource(id = R.string.new_maintenance_task),
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
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        enabled = itemName.isNotBlank() && !isLoading,
                        onClick = {
                            isLoading = true

                            val newMaintenanceTask =
                                datePickerState.selectedDateMillis?.toLocalDate()?.let {
                                    val eventDate = it.plusMonths(itemPeriodicity.toLong())
                                    Item(
                                        name = itemName,
                                        periodicity = itemPeriodicity.toInt(),
                                        date = it.toString(),
                                        eventID = insertEvent(context, eventDate, itemName)
                                    )
                                }

                            if (newMaintenanceTask != null) {
                                scope.launch {
                                    viewModel.addNewItem(newMaintenanceTask)
                                        .collect {
                                            isLoading = false

                                            withContext(Dispatchers.Main) {
                                                if (it) {
                                                    Toast.makeText(
                                                        context,
                                                        R.string.add_maintenance_task_result,
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
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.generic_error_toast,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.create))
                    }
                }
            }
        }
    }
}