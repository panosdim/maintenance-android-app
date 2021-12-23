package com.panosdim.maintenance.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panosdim.maintenance.R
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.model.data.items
import com.panosdim.maintenance.ui.theme.MaintenanceTheme
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun ItemCard(item: Item) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val resources = LocalContext.current.resources
    val periodicityText = resources.getQuantityString(
        R.plurals.periodicity,
        item.periodicity as Int,
        item.periodicity
    )
    val maintenanceDate = item.date.plusYears(item.periodicity.toLong())
    Card(
        modifier = Modifier
            // The space between each card and the other
            .padding(4.dp)
            .fillMaxWidth()
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
                        item.date.format(dateFormatter)
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun ItemCardPreview() {
    MaintenanceTheme {
        ItemCard(items[0])
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun ItemCard1Preview() {
    MaintenanceTheme {
        ItemCard(items[1])
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun ItemCard2Preview() {
    MaintenanceTheme {
        ItemCard(items[2])
    }
}