package com.panosdim.maintenance.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import com.panosdim.maintenance.model.Item
import kotlinx.coroutines.CoroutineScope

@ExperimentalMaterialApi
@Composable
fun ItemForm(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    maintenanceItem: Item?
) {
    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            LazyColumn {
                items(50) {
                    ListItem(
                        text = { Text("Item $it") },
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Localized description"
                            )
                        }
                    )
                }
            }
        }
    ) {}
}