package com.panosdim.maintenance.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.panosdim.maintenance.ItemDetailsActivity
import com.panosdim.maintenance.LoginActivity
import com.panosdim.maintenance.R
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.user
import com.panosdim.maintenance.utils.toLocalDate
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceItems(maintenanceItems: List<Item>) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    FilledIconButton(
                        onClick = {
                            AuthUI.getInstance()
                                .signOut(context)
                                .addOnCompleteListener {
                                    user = null
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                        },
                    ) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    context.startActivity(intent)
                },
                content = {
                    Icon(Icons.Filled.Add, null)
                })
        }

    ) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentPadding = it
        ) {
            items(maintenanceItems) { item ->
                Box(contentAlignment = Alignment.TopEnd) {
                    ItemCard(item)
                    val maintenanceDate =
                        item.date.toLocalDate().plusMonths(item.periodicity.toLong())
                    if (maintenanceDate.isBefore(LocalDate.now())) {
                        Icon(
                            Icons.Filled.Error,
                            null,
                            tint = Color.Red,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Color.Green,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

