package com.panosdim.maintenance.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.maintenance.LoginActivity
import com.panosdim.maintenance.R
import com.panosdim.maintenance.data.MainViewModel
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.onComplete
import com.panosdim.maintenance.paddingLarge
import com.panosdim.maintenance.utils.formatDuration
import com.panosdim.maintenance.utils.getPeriodicity
import com.panosdim.maintenance.utils.toFormattedString
import com.panosdim.maintenance.utils.toLocalDate
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val listStatenNeedMaintenance = rememberLazyListState()
    val listStateMaintenanceTasks = rememberLazyListState()
    val today = LocalDate.now()
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(Item()) }
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val editMaintenanceTaskSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val newMaintenanceTaskSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val items by viewModel.getMaintenanceItems()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val itemsNeedMaintenance = items.filter {
        val maintenanceDate =
            it.date.toLocalDate().plusMonths(it.periodicity.toLong())
        maintenanceDate.isBefore(LocalDate.now())
    }

    val maintenanceItems = items.filter {
        val maintenanceDate =
            it.date.toLocalDate().plusMonths(it.periodicity.toLong())
        maintenanceDate.isAfter(today)
    }.sortedBy { it.date.toLocalDate().plusMonths(it.periodicity.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (itemsNeedMaintenance.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .padding(paddingLarge)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = paddingLarge),
                                text = stringResource(id = R.string.items_need_maintenance),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium,
                            )

                            // Show Items
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    horizontal = paddingLarge,
                                    vertical = paddingLarge
                                ),
                                state = listStatenNeedMaintenance
                            ) {
                                if (itemsNeedMaintenance.isNotEmpty()) {
                                    itemsNeedMaintenance.iterator().forEachRemaining { item ->
                                        item {
                                            val maintenanceDate = item.date.toLocalDate()
                                                .plusMonths(item.periodicity.toLong())
                                            ListItem(
                                                headlineContent = {
                                                    Text(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center,
                                                        text = item.name,
                                                        style = MaterialTheme.typography.headlineSmall
                                                    )
                                                },
                                                supportingContent = {
                                                    Column {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = getPeriodicity(
                                                                    item.periodicity.toFloat(),
                                                                    resources
                                                                ),
                                                                style = MaterialTheme.typography.titleSmall,
                                                            )
                                                            Text(
                                                                text = stringResource(
                                                                    R.string.last_maintenance_date,
                                                                    item.date.toLocalDate()
                                                                        .toFormattedString()
                                                                ),
                                                                style = MaterialTheme.typography.titleMedium,
                                                            )
                                                        }
                                                        
                                                        Text(
                                                            text = stringResource(
                                                                R.string.delayed_days,
                                                                formatDuration(
                                                                    Duration.between(
                                                                        maintenanceDate.atStartOfDay(),
                                                                        LocalDate.now()
                                                                            .atStartOfDay()
                                                                    ), resources
                                                                )
                                                            ),
                                                            style = MaterialTheme.typography.titleMedium,
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.clickable {
                                                    selectedItem = item
                                                    scope.launch { editMaintenanceTaskSheetState.show() }
                                                }
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                } else {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = stringResource(id = R.string.no_maintenance_items),
                                                modifier = Modifier
                                            )
                                            Text(
                                                text = stringResource(id = R.string.no_maintenance_items)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(paddingLarge)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = paddingLarge),
                            text = stringResource(id = R.string.app_name),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        // Show Items
                        LazyColumn(
                            Modifier
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                horizontal = paddingLarge,
                                vertical = paddingLarge
                            ),
                            state = listStateMaintenanceTasks
                        ) {
                            if (maintenanceItems.isNotEmpty()) {
                                maintenanceItems.iterator().forEachRemaining { item ->
                                    item {
                                        val maintenanceDate = item.date.toLocalDate()
                                            .plusMonths(item.periodicity.toLong())

                                        ListItem(
                                            headlineContent = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = item.name,
                                                        style = MaterialTheme.typography.headlineSmall
                                                    )
                                                    Text(
                                                        text = getPeriodicity(
                                                            item.periodicity.toFloat(),
                                                            resources
                                                        ),
                                                        style = MaterialTheme.typography.titleSmall,
                                                    )
                                                }

                                            },
                                            supportingContent = {
                                                Column {
                                                    Text(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center,
                                                        text = stringResource(R.string.maintenance),
                                                        style = MaterialTheme.typography.titleMedium,
                                                    )
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = stringResource(
                                                                R.string.last_maintenance_date,
                                                                item.date.toLocalDate()
                                                                    .toFormattedString()
                                                            ),
                                                            style = MaterialTheme.typography.titleMedium,
                                                        )
                                                        Text(
                                                            text = stringResource(
                                                                R.string.next_maintenance,
                                                                maintenanceDate.toFormattedString()
                                                            ),
                                                            style = MaterialTheme.typography.titleMedium,
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.clickable {
                                                selectedItem = item
                                                scope.launch { editMaintenanceTaskSheetState.show() }
                                            }
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            } else {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = stringResource(id = R.string.no_maintenance_items),
                                            modifier = Modifier
                                        )
                                        Text(
                                            text = stringResource(id = R.string.no_maintenance_items)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .padding(paddingLarge)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            context.unregisterReceiver(onComplete)
                            Firebase.auth.signOut()

                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.logout))
                    }

                    Button(
                        onClick = {
                            scope.launch { newMaintenanceTaskSheetState.show() }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.add_new_maintenance_task))
                    }
                }
            }
        }

        NewMaintenanceTaskSheet(bottomSheetState = newMaintenanceTaskSheetState)
        EditMaintenanceTaskSheet(
            item = selectedItem,
            bottomSheetState = editMaintenanceTaskSheetState
        )
    }
}