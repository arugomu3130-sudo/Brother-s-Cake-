package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInventoryDialog(
    item: InventoryItem? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, quantity: Double, unit: String, price: Double, minThreshold: Double) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Cakes") }
    var quantityText by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var priceText by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var minThresholdText by remember { mutableStateOf(item?.minThreshold?.toString() ?: "3.0") }

    val categories = listOf("Cakes", "Pastries", "Bread", "Cookies", "Other")
    val units = listOf("pcs", "box", "kg", "g")

    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var minThresholdError by remember { mutableStateOf(false) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Inventory Item" else "Edit Inventory Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Item Name") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_name_input"),
                    supportingText = { if (nameError) Text("Name cannot be empty") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = category,
                            onValueChange = {},
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        category = selectionOption
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = unit,
                            onValueChange = {},
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        unit = selectionOption
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it
                            quantityError = false
                        },
                        label = { Text("Stock Level") },
                        isError = quantityError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_quantity_input"),
                        supportingText = { if (quantityError) Text("Invalid number") }
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            priceError = false
                        },
                        label = { Text("Unit Price ($)") },
                        isError = priceError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_price_input"),
                        supportingText = { if (priceError) Text("Invalid price") }
                    )
                }

                OutlinedTextField(
                    value = minThresholdText,
                    onValueChange = {
                        minThresholdText = it
                        minThresholdError = false
                    },
                    label = { Text("Low Stock Alert Threshold") },
                    isError = minThresholdError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_threshold_input"),
                    supportingText = { if (minThresholdError) Text("Invalid threshold") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.trim()
                    val finalQty = quantityText.toDoubleOrNull()
                    val finalPrice = priceText.toDoubleOrNull()
                    val finalThreshold = minThresholdText.toDoubleOrNull()

                    if (finalName.isEmpty()) nameError = true
                    if (finalQty == null || finalQty < 0.0) quantityError = true
                    if (finalPrice == null || finalPrice < 0.0) priceError = true
                    if (finalThreshold == null || finalThreshold < 0.0) minThresholdError = true

                    if (!nameError && !quantityError && !priceError && !minThresholdError) {
                        onSave(
                            finalName,
                            category,
                            finalQty!!,
                            unit,
                            finalPrice!!,
                            finalThreshold!!
                        )
                    }
                },
                modifier = Modifier.testTag("inventory_save_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("inventory_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
