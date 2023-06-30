package com.panosdim.maintenance

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.ui.ItemForm
import com.panosdim.maintenance.ui.theme.MaintenanceTheme

@Suppress("DEPRECATION")
class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var item: Item
    private val bundle: Bundle? by lazy { intent.extras }
    private val itemsViewModel by viewModels<ItemsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.let {
                item = it.getParcelable(MSG.ITEM.message, Item::class.java) as Item
            }
        } else {
            bundle?.let {
                item = it.getParcelable<Item>(MSG.ITEM.message) as Item
            }
        }

        setContent {
            MaintenanceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (this::item.isInitialized) {
                        ItemForm(maintenanceItem = item, itemsViewModel)
                    } else {
                        ItemForm(maintenanceItem = null, itemsViewModel)
                    }
                }
            }
        }
    }
}