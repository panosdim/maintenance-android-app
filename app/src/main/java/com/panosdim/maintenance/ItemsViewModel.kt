package com.panosdim.maintenance

import androidx.lifecycle.ViewModel
import com.panosdim.maintenance.model.Item

class ItemsViewModel : ViewModel() {
    private val repository = Repository()
    val maintenanceItems = repository.getItems()

    fun addNewItem(item: Item) {
        repository.addNewItem(item)
    }

    fun updateItem(item: Item) {
        repository.updateItem(item)
    }

    fun removeItem(item: Item) {
        repository.deleteItem(item)
    }
}