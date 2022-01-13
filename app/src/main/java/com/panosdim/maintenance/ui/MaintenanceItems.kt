package com.panosdim.maintenance.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.panosdim.maintenance.*
import com.panosdim.maintenance.R
import java.time.LocalDate


@Composable
fun MaintenanceItems(itemsViewModel: ItemsViewModel) {
    val context = LocalContext.current
    val maintenanceItems = itemsViewModel.maintenanceItems.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
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
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    context.startActivity(intent)
                },
                backgroundColor = MaterialTheme.colors.secondary,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        tint = Color.White
                    )
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
                    if (item.date.toLocalDate().plusYears(item.periodicity.toLong())
                            .isBefore(LocalDate.now())
                    ) {
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
    }
}

