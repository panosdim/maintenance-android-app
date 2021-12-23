package com.panosdim.maintenance

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.panosdim.maintenance.model.data.items
import com.panosdim.maintenance.ui.MaintenanceItems
import com.panosdim.maintenance.ui.theme.MaintenanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaintenanceTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    //TODO: Fetch data from firebase
                    MaintenanceItems(maintenanceItems = items)
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun DefaultPreview() {
    MaintenanceTheme {
        Surface(color = MaterialTheme.colors.background) {
            MaintenanceItems(maintenanceItems = items)
        }
    }
}