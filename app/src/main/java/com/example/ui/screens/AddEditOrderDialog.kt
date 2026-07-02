package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CustomerOrder
import com.example.data.InventoryItem
import com.example.data.OrderItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderDialog(
    order: CustomerOrder? = null,
    availableInventory: List<InventoryItem>,
    onDismiss: () -> Unit,
    onSave: (
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        pickupDate: Long,
        items: List<OrderItem>,
        totalPrice: Double,
        notes: String,
        status: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf(order?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(order?.customerPhone ?: "") }
    var customerEmail by remember { mutableStateOf(order?.customerEmail ?: "") }
    var notes by remember { mutableStateOf(order?.notes ?: "") }
    var status by remember { mutableStateOf(order?.status ?: "PENDING") }

    // Selected items map: itemId -> ordered quantity
    val selectedItems = remember {
        mutableStateMapOf<Int, Double>().apply {
            order?.items?.forEach {
                put(it.itemId, it.quantity)
            }
        }
    }

    var customerNameError by remember { mutableStateOf(false) }
    var customerPhoneError by remember { mutableStateOf(false) }
    var itemsError by remember { mutableStateOf(false) }

    // Default pickup date: tomorrow at 12:00 PM if new, or stored date
    val calendar = remember {
        Calendar.getInstance().apply {
            if (order != null) {
                timeInMillis = order.pickupDate
            } else {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
            }
        }
    }

    var pickupDateText by remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        mutableStateOf(sdf.format(calendar.time))
    }
    var pickupDateError by remember { mutableStateOf(false) }

    // Calculate total price dynamically
    val totalPrice by remember {
        derivedStateOf {
            selectedItems.entries.sumOf { (itemId, qty) ->
                val invItem = availableInventory.find { it.id == itemId }
                (invItem?.price ?: 0.0) * qty
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = if (order == null) "Create Customer Order" else "Edit Order #${order.orderId}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Customer Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = {
                                        customerName = it
                                        customerNameError = false
                                    },
                                    label = { Text("Customer Name") },
                                    isError = customerNameError,
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("order_customer_name"),
                                    supportingText = { if (customerNameError) Text("Name is required") }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = {
                                            customerPhone = it
                                            customerPhoneError = false
                                        },
                                        label = { Text("Phone Number") },
                                        isError = customerPhoneError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("order_customer_phone"),
                                        supportingText = { if (customerPhoneError) Text("Phone is required") }
                                    )

                                    OutlinedTextField(
                                        value = customerEmail,
                                        onValueChange = { customerEmail = it },
                                        label = { Text("Email (Optional)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("order_customer_email")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Pickup / Delivery Schedule",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                OutlinedTextField(
                                    value = pickupDateText,
                                    onValueChange = {
                                        pickupDateText = it
                                        pickupDateError = false
                                    },
                                    label = { Text("Pickup Date & Time (YYYY-MM-DD HH:MM)") },
                                    isError = pickupDateError,
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("order_pickup_date"),
                                    supportingText = {
                                        if (pickupDateError) {
                                            Text("Use format: YYYY-MM-DD HH:MM")
                                        } else {
                                            Text("Format: YYYY-MM-DD HH:MM (e.g. 2026-07-02 14:30)")
                                        }
                                    }
                                )

                                // Status Dropdown for Editing
                                Text("Order Status", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val statusOptions = listOf("PENDING", "IN_PROGRESS", "READY", "COMPLETED", "CANCELLED")
                                    statusOptions.forEach { statusOption ->
                                        val isSelected = status == statusOption
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { status = statusOption },
                                            label = { Text(statusOption, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Select Items to Order",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (itemsError) {
                            Text(
                                "Please select at least one item to order",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Available inventory items
                    if (availableInventory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No inventory items found. Add items first!", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(availableInventory) { invItem ->
                            val currentQty = selectedItems[invItem.id] ?: 0.0
                            val exceedsStock = currentQty > invItem.quantity

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (exceedsStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        if (currentQty > 0) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        invItem.name,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "$${String.format("%.2f", invItem.price)} / ${invItem.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "• Stock: ${invItem.quantity} ${invItem.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (invItem.isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (exceedsStock) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Low Stock Alert",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Exceeds available stock!",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (currentQty > 0) {
                                                val nextQty = currentQty - 1.0
                                                if (nextQty <= 0.0) {
                                                    selectedItems.remove(invItem.id)
                                                } else {
                                                    selectedItems[invItem.id] = nextQty
                                                }
                                                itemsError = false
                                            }
                                        },
                                        enabled = currentQty > 0
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease Ordered Quantity")
                                    }

                                    Text(
                                        text = if (currentQty == 0.0) "0" else currentQty.toInt().toString(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    IconButton(
                                        onClick = {
                                            selectedItems[invItem.id] = currentQty + 1.0
                                            itemsError = false
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase Ordered Quantity")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Custom Notes (Writing, Allergies, Gift etc.)") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("order_notes")
                        )
                    }
                }

                // Footer with totals and action buttons
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("order_total_price")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                var parsedTime: Long = 0
                                try {
                                    val d = sdf.parse(pickupDateText.trim())
                                    if (d != null) {
                                        parsedTime = d.time
                                    } else {
                                        pickupDateError = true
                                    }
                                } catch (e: Exception) {
                                    pickupDateError = true
                                }

                                if (customerName.trim().isEmpty()) customerNameError = true
                                if (customerPhone.trim().isEmpty()) customerPhoneError = true
                                if (selectedItems.isEmpty()) itemsError = true

                                if (!customerNameError && !customerPhoneError && !pickupDateError && !itemsError) {
                                    // Compile order items list
                                    val orderItems = selectedItems.mapNotNull { (itemId, qty) ->
                                        val invItem = availableInventory.find { it.id == itemId }
                                        if (invItem != null) {
                                            OrderItem(
                                                itemId = itemId,
                                                name = invItem.name,
                                                quantity = qty,
                                                unit = invItem.unit,
                                                priceAtOrder = invItem.price
                                            )
                                        } else null
                                    }

                                    onSave(
                                        customerName,
                                        customerPhone,
                                        customerEmail,
                                        parsedTime,
                                        orderItems,
                                        totalPrice,
                                        notes,
                                        status
                                    )
                                }
                            },
                            modifier = Modifier.testTag("order_submit_button")
                        ) {
                            Text("Save Order")
                        }
                    }
                }
            }
        }
    }
}
