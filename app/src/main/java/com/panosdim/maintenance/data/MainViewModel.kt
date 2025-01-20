package com.panosdim.maintenance.data

import androidx.lifecycle.ViewModel
import com.panosdim.maintenance.model.Item

class MainViewModel : ViewModel() {
    private val repository = Repository()

    fun getMaintenanceItems() = repository.getItems()

    fun addNewItem(item: Item) = repository.addNewItem(item)

    fun updateItem(item: Item) = repository.updateItem(item)

    fun removeItem(item: Item) = repository.deleteItem(item)
}