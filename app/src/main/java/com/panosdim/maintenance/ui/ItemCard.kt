package com.panosdim.maintenance.ui

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.*
import com.panosdim.maintenance.R
import com.panosdim.maintenance.model.Item
import java.time.Duration
import java.time.LocalDate


@Composable
fun ItemCard(item: Item) {
    val context = LocalContext.current
    val resources = context.resources
    val periodicityText = resources.getQuantityString(
        R.plurals.periodicity,
        item.periodicity,
        item.periodicity
    )
    val maintenanceDate = item.date.toLocalDate().plusYears(item.periodicity.toLong())

    Card(
        modifier = Modifier
            // The space between each card and the other
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                val intent = Intent(context, ItemDetailsActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable(MSG.ITEM.message, item)
                intent.putExtra(MSG.ITEM.message, item)
                context.startActivity(intent)
            })
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = periodicityText,
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(
                        R.string.maintenance_date,
                        item.date.toLocalDate().toFormattedString()
                    ),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (maintenanceDate.isBefore(LocalDate.now())) {
                    Text(
                        text = stringResource(
                            R.string.delayed_days,
                            Duration.between(
                                maintenanceDate.atStartOfDay(),
                                LocalDate.now().atStartOfDay()
                            ).toDays()
                        ),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}