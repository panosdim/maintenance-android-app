package com.panosdim.maintenance

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.panosdim.maintenance.model.Item

class ItemsViewModel : ViewModel() {
    var maintenanceItems = mutableStateListOf<Item>()
        private set

    fun addItem(item: Item) {
        maintenanceItems.add(item)
    }

    fun removeItem(item: Item) {
        maintenanceItems.remove(item)
    }

    fun updateItem(item: Item) {
        val index = maintenanceItems.indexOfFirst { itm -> itm.id == item.id }
        maintenanceItems[index] = item
    }

}