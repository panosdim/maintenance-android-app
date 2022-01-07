package com.panosdim.maintenance

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.ui.ItemForm
import com.panosdim.maintenance.ui.theme.MaintenanceTheme

class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var item: Item
    private val bundle: Bundle? by lazy { intent.extras }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (bundle != null) {
            item = bundle!!.getParcelable<Parcelable>(MSG.ITEM.message) as Item
        }

        setContent {
            MaintenanceTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    if (this::item.isInitialized) {
                        ItemForm(maintenanceItem = item)
                    } else {
                        ItemForm(maintenanceItem = null)
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun DefaultPreviewItemDetails() {
    MaintenanceTheme {
        Surface(color = MaterialTheme.colors.background) {
            ItemForm(maintenanceItem = null)
        }
    }
}