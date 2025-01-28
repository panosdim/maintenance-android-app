package com.panosdim.maintenance.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
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

    val expandedFab by remember {
        derivedStateOf {
            listStateMaintenanceTasks.firstVisibleItemIndex == 0
        }
    }

    val showSpacer by remember {
        derivedStateOf {
            listStateMaintenanceTasks.layoutInfo.visibleItemsInfo.lastOrNull()?.index == items.lastIndex
        }
    }

    val maintenanceItems = items.filter {
        val maintenanceDate =
            it.date.toLocalDate().plusMonths(it.periodicity.toLong())
        maintenanceDate.isAfter(today)
    }.sortedBy { it.date.toLocalDate().plusMonths(it.periodicity.toLong()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                actions = {
                    IconButton(onClick = {
                        context.unregisterReceiver(onComplete)
                        Firebase.auth.signOut()

                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { scope.launch { newMaintenanceTaskSheetState.show() } },
                expanded = expandedFab,
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        stringResource(id = R.string.new_maintenance_task)
                    )
                },
                text = { Text(text = stringResource(id = R.string.new_maintenance_task)) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .padding(paddingLarge)
                    .fillMaxWidth()
                    .weight(1f)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
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
                    if (maintenanceItems.isNotEmpty() || itemsNeedMaintenance.isNotEmpty()) {
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
                                    trailingContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .wrapContentWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            selectedItem = item
                                            scope.launch { editMaintenanceTaskSheetState.show() }
                                        }
                                        .height(IntrinsicSize.Min),
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                                )
                                if (item != itemsNeedMaintenance.lastOrNull() || maintenanceItems.isNotEmpty()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        maintenanceItems.iterator().forEachRemaining { item ->
                            item {
                                val maintenanceDate = item.date.toLocalDate()
                                    .plusMonths(item.periodicity.toLong())

                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                    },
                                    supportingContent = {
                                        Column {
                                            Text(
                                                text = getPeriodicity(
                                                    item.periodicity.toFloat(),
                                                    resources
                                                ),
                                                style = MaterialTheme.typography.titleSmall,
                                            )
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
                                    trailingContent = {
                                        // Calculate days until next maintenance
                                        val daysUntilNextMaintenance = ChronoUnit.DAYS.between(
                                            LocalDate.now(),
                                            maintenanceDate
                                        )

                                        // Choose the appropriate icon and color
                                        val (icon, tint) = if (daysUntilNextMaintenance <= 30) {
                                            Icons.Filled.Warning to Color.Yellow
                                        } else {
                                            Icons.Filled.CheckCircle to Color.Green
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .wrapContentWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = tint,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            selectedItem = item
                                            scope.launch { editMaintenanceTaskSheetState.show() }
                                        }
                                        .height(IntrinsicSize.Min),
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                                )
                                if (item != maintenanceItems.lastOrNull()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                                }
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
            if (showSpacer) {
                Spacer(modifier = Modifier.padding(bottom = 70.dp))
            }
        }

        NewMaintenanceTaskSheet(bottomSheetState = newMaintenanceTaskSheetState)
        EditMaintenanceTaskSheet(
            item = selectedItem,
            bottomSheetState = editMaintenanceTaskSheetState
        )
    }
}