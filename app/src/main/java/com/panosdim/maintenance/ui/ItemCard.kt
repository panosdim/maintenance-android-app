package com.panosdim.maintenance.ui

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.ItemDetailsActivity
import com.panosdim.maintenance.MSG
import com.panosdim.maintenance.R
import com.panosdim.maintenance.formatDuration
import com.panosdim.maintenance.getPeriodicity
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.toFormattedString
import com.panosdim.maintenance.toLocalDate
import java.time.Duration
import java.time.LocalDate


@Composable
fun ItemCard(item: Item) {
    val context = LocalContext.current
    val resources = context.resources
    val periodicityText = getPeriodicity(item.periodicity.toFloat(), resources)
    val maintenanceDate = item.date.toLocalDate().plusMonths(item.periodicity.toLong())

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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = periodicityText,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(
                        R.string.maintenance_date,
                        item.date.toLocalDate().toFormattedString()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (maintenanceDate.isBefore(LocalDate.now())) {
                    Text(
                        text = stringResource(
                            R.string.delayed_days,
                            formatDuration(
                                Duration.between(
                                    maintenanceDate.atStartOfDay(),
                                    LocalDate.now().atStartOfDay()
                                ), resources
                            )
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}