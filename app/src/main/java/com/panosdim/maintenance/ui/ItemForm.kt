package com.panosdim.maintenance.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.R
import com.panosdim.maintenance.model.Item
import java.time.LocalDate
import kotlin.math.nextUp

@Composable
fun ItemForm(
    maintenanceItem: Item?
) {
    val activity = (LocalContext.current as? Activity)

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text(stringResource(R.string.item_details)) },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("ITEM_FORM", "EXIT CLICKED")
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        activity?.finish()
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), "Back")
                    }
                },
            )
        }
    ) {
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
                return@rememberSaveable mutableStateOf(1f)
            }
        }
        var itemLastMaintenance by rememberSaveable {
            maintenanceItem?.let {
                return@rememberSaveable mutableStateOf(it.date)
            } ?: run {
                return@rememberSaveable mutableStateOf(LocalDate.now())
            }
        }
        val onDateChange = { date: LocalDate ->
            itemLastMaintenance = date
        }

        val openDialog = remember { mutableStateOf(false) }

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
                            // TODO: DELETE item in firebase.
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

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
            )
            Text(
                text = "Periodicity ${itemPeriodicity.toInt()}"
            )
            Slider(
                value = itemPeriodicity,
                onValueChange = { itemPeriodicity = it.nextUp() },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.secondary,
                    activeTrackColor = MaterialTheme.colors.secondary
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Last Maintenance"
            )
            DatePicker(itemLastMaintenance, onDateChange)

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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Delete")
                    }
                    Button(
                        onClick = { /* TODO: UPDATE ITEM */ },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Update")
                    }
                }
            } ?: run {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Button(
                        onClick = { /* TODO: ADD ITEM */ },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
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