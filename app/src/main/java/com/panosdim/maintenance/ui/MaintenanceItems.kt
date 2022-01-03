package com.panosdim.maintenance.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.R
import com.panosdim.maintenance.model.Item
import kotlinx.coroutines.launch
import java.time.LocalDate


@ExperimentalMaterialApi
@Composable
fun MaintenanceItems(maintenanceItems: List<Item>) {
    val TAG = "MaintenanceItems"
    val scope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d(TAG, "EXIT CLICKED")
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        if (bottomSheetScaffoldState.isVisible) {
                            bottomSheetScaffoldState.hide()
                        } else {
                            bottomSheetScaffoldState.show()
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.secondary,
                content = {
                    if (bottomSheetScaffoldState.isVisible) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            tint = Color.White
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                })
        }

    ) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(maintenanceItems) { item ->
                Box(contentAlignment = Alignment.TopEnd) {
                    ItemCard(item)
                    if (item.date.plusYears(item.periodicity.toLong()).isBefore(LocalDate.now())) {
                        Icon(
                            modifier = Modifier.padding(12.dp),
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = null,
                            tint = Color.Red
                        )
                    } else {
                        Icon(
                            modifier = Modifier.padding(12.dp),
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = Color.Green
                        )
                    }
                }

            }
        }
        ItemForm(scope, bottomSheetScaffoldState, maintenanceItem = null)
    }
}

