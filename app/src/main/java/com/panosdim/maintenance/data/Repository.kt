package com.panosdim.maintenance.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.panosdim.maintenance.database
import com.panosdim.maintenance.model.Item
import com.panosdim.maintenance.user
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Repository {
    fun getItems(): Flow<List<Item>> = callbackFlow {
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
            channel.close()
            if (listener != null) {
                itemsRef.removeEventListener(listener)
            }
        }
    }

    fun deleteItem(item: Item): Flow<Boolean> = callbackFlow {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }
        itemsRef?.removeValue()
            ?.addOnSuccessListener {
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }

    fun addNewItem(item: Item): Flow<Boolean> = callbackFlow {
        val itemsRef = user?.let { database.getReference("items").child(it.uid) }

        itemsRef?.push()?.setValue(item)
            ?.addOnSuccessListener {
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }

    fun updateItem(item: Item): Flow<Boolean> = callbackFlow {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        itemsRef?.setValue(item)
            ?.addOnSuccessListener {
                itemsRef.child("id").removeValue()
                trySend(true)
                cancel()
            }
            ?.addOnFailureListener {
                trySend(false)
                cancel()
            }

        awaitClose {
            channel.close()
        }
    }
}