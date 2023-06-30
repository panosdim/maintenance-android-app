package com.panosdim.maintenance

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.panosdim.maintenance.model.Item
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Repository {
    fun getItems(): Flow<List<Item>> {
        return callbackFlow {
            val itemsRef = user?.let { database.getReference("items").child(it.uid) }

            val listener =
                itemsRef?.orderByChild("date")?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val items = mutableListOf<Item>()
                        snapshot.children.forEach {
                            val item = it.getValue(Item::class.java)
                            if (item != null) {
                                item.id = it.key
                                items.add(item)
                            }
                        }
                        // Emit the user data to the flow
                        trySend(items)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cancel()
                    }

                })

            awaitClose {
                if (listener != null) {
                    itemsRef.removeEventListener(listener)
                }
            }
        }
    }

    fun deleteItem(item: Item) {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }
        itemsRef?.removeValue()
    }

    fun addNewItem(item: Item) {
        val itemsRef = user?.let { database.getReference("items").child(it.uid) }

        itemsRef?.push()?.setValue(item)
    }

    fun updateItem(item: Item) {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        itemsRef?.setValue(item)
        itemsRef?.child("id")?.removeValue()
    }
}