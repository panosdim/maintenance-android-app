package com.panosdim.maintenance.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.ItemsViewModel
import com.panosdim.maintenance.R
import com.panosdim.maintenance.TAG
import com.panosdim.maintenance.getPeriodicity
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.toEpochMilli
import com.panosdim.maintenance.toLocalDate
import java.time.LocalDate
import kotlin.math.nextUp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemForm(
    maintenanceItem: Item?,
    viewModel: ItemsViewModel
) {
    val context = LocalContext.current
    val resources = context.resources
    val activity = (context as? Activity)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.item_details)) },
                navigationIcon = {
                    IconButton(onClick = {
                        activity?.finish()
                    }) {
                        Icon(Icons.Outlined.ArrowBack, "Back")
                    }
                },
            )
        }
    ) { contentPadding ->
        var itemName by rememberSaveable {
            maintenanceItem?.let {
                return@rememberSaveable mutableStateOf(it.name)
            } ?: run {
                return@rememberSaveable mutableStateOf("")
            }
        }
        var itemPeriodicity by rememberSaveable {
            maintenanceItem?.let {
                return@rememberSaveable mutableStateOf(it.periodicity.toFloat())
            } ?: run {
                return@rememberSaveable mutableStateOf(12f)
            }
        }
        val itemLastMaintenance by rememberSaveable {
            maintenanceItem?.let {
                return@rememberSaveable mutableStateOf(LocalDate.parse(it.date))
            } ?: run {
                return@rememberSaveable mutableStateOf(LocalDate.now())
            }
        }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = itemLastMaintenance.toEpochMilli())

        val openDialog = remember { mutableStateOf(false) }

        var periodicityText by rememberSaveable {
            return@rememberSaveable mutableStateOf(getPeriodicity(itemPeriodicity, resources))
        }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    Text(text = "Delete Item")
                },
                text = {
                    Text(
                        "Item will be deleted permanently and will not be able to recover it."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                            if (maintenanceItem != null) {
                                viewModel.removeItem(maintenanceItem)
                            }
                            Toast.makeText(
                                context, "Item Deleted Successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                            activity?.finish()
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            )
        }

        fun isFormValid(): Boolean {
            return itemName.isNotBlank()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .padding(8.dp),
        ) {
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
                isError = !isFormValid(),
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
            )
            Text(
                text = periodicityText
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

            OutlinedDatePicker(
                state = datePickerState,
                label = stringResource(id = R.string.last_maintenance)
            )

            maintenanceItem?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = { openDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Delete")
                    }
                    Button(
                        enabled = isFormValid(),
                        onClick = {
                            maintenanceItem.name = itemName
                            maintenanceItem.periodicity = itemPeriodicity.toInt()
                            datePickerState.selectedDateMillis?.toLocalDate()?.let {
                                maintenanceItem.date = it.toString()
                            }

                            viewModel.updateItem(maintenanceItem)

                            Toast.makeText(
                                context, "Item Updated Successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                            activity?.finish()
                        },
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Update")
                    }
                }
                Log.d(TAG, "Update Item form")
            } ?: kotlin.run {
                Log.d(TAG, "New Item form")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Button(
                        enabled = isFormValid(),
                        onClick = {
                            val newItem = datePickerState.selectedDateMillis?.toLocalDate()?.let {
                                Item(
                                    name = itemName,
                                    periodicity = itemPeriodicity.toInt(),
                                    date = it.toString()
                                )
                            }

                            if (newItem != null) {
                                viewModel.addNewItem(newItem)
                                Toast.makeText(
                                    context, "Item Saved Successfully.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context, "Failed to save new item.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            activity?.finish()
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Create")
                    }
                }
            }
        }
    }
}