package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class BakeryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BakeryRepository

    val inventoryItems: StateFlow<List<InventoryItem>>
    val orders: StateFlow<List<CustomerOrder>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BakeryRepository(database.bakeryDao())

        inventoryItems = repository.allInventoryItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed sample data if database is empty
        viewModelScope.launch {
            inventoryItems.filter { it.isNotEmpty() }.firstOrNull() ?: run {
                // If inventory is empty, seed it
                seedSampleData()
            }
        }
    }

    private suspend fun seedSampleData() {
        val sampleItems = listOf(
            InventoryItem(
                name = "Strawberry Rose Cake",
                category = "Cakes",
                quantity = 8.0,
                unit = "pcs",
                price = 45.00,
                minThreshold = 3.0
            ),
            InventoryItem(
                name = "Triple Chocolate Mousse",
                category = "Cakes",
                quantity = 5.0,
                unit = "pcs",
                price = 38.00,
                minThreshold = 2.0
            ),
            InventoryItem(
                name = "Almond Butter Croissant",
                category = "Pastries",
                quantity = 18.0,
                unit = "pcs",
                price = 4.50,
                minThreshold = 5.0
            ),
            InventoryItem(
                name = "Classic Sourdough Boule",
                category = "Bread",
                quantity = 15.0,
                unit = "pcs",
                price = 7.50,
                minThreshold = 4.0
            ),
            InventoryItem(
                name = "Gourmet Pecan Cookies Box",
                category = "Cookies",
                quantity = 12.0,
                unit = "box",
                price = 12.00,
                minThreshold = 3.0
            ),
            InventoryItem(
                name = "Vanilla Bean Macaron",
                category = "Pastries",
                quantity = 30.0,
                unit = "pcs",
                price = 2.50,
                minThreshold = 8.0
            )
        )

        val insertedIds = mutableListOf<Long>()
        for (item in sampleItems) {
            val id = repository.insertInventoryItem(item)
            insertedIds.add(id)
        }

        // Add some sample orders if empty
        orders.filter { it.isNotEmpty() }.firstOrNull() ?: run {
            val now = Calendar.getInstance()
            
            // Order 1: Ready in 2 hours
            val pickup1 = now.clone() as Calendar
            pickup1.add(Calendar.HOUR, 2)
            val order1 = CustomerOrder(
                customerName = "Emily Johnson",
                customerPhone = "(555) 123-4567",
                customerEmail = "emily@example.com",
                pickupDate = pickup1.timeInMillis,
                items = listOf(
                    OrderItem(
                        itemId = insertedIds.getOrNull(0)?.toInt() ?: 1,
                        name = "Strawberry Rose Cake",
                        quantity = 1.0,
                        unit = "pcs",
                        priceAtOrder = 45.00
                    )
                ),
                totalPrice = 45.00,
                notes = "Writing: 'Happy Birthday Mom!' in pink frosting.",
                status = "READY"
            )

            // Order 2: Tomorrow morning
            val pickup2 = now.clone() as Calendar
            pickup2.add(Calendar.DAY_OF_YEAR, 1)
            pickup2.set(Calendar.HOUR_OF_DAY, 9)
            pickup2.set(Calendar.MINUTE, 0)
            val order2 = CustomerOrder(
                customerName = "David Chen",
                customerPhone = "(555) 987-6543",
                customerEmail = "david.c@example.com",
                pickupDate = pickup2.timeInMillis,
                items = listOf(
                    OrderItem(
                        itemId = insertedIds.getOrNull(2)?.toInt() ?: 3,
                        name = "Almond Butter Croissant",
                        quantity = 4.0,
                        unit = "pcs",
                        priceAtOrder = 4.50
                    ),
                    OrderItem(
                        itemId = insertedIds.getOrNull(3)?.toInt() ?: 4,
                        name = "Classic Sourdough Boule",
                        quantity = 1.0,
                        unit = "pcs",
                        priceAtOrder = 7.50
                    )
                ),
                totalPrice = 25.50,
                notes = "Please slice the Sourdough Boule.",
                status = "IN_PROGRESS"
            )

            // Order 3: Next Friday afternoon
            val pickup3 = now.clone() as Calendar
            pickup3.add(Calendar.DAY_OF_YEAR, 3)
            val order3 = CustomerOrder(
                customerName = "Sarah Miller",
                customerPhone = "(555) 456-7890",
                customerEmail = "sarah.m@example.com",
                pickupDate = pickup3.timeInMillis,
                items = listOf(
                    OrderItem(
                        itemId = insertedIds.getOrNull(4)?.toInt() ?: 5,
                        name = "Gourmet Pecan Cookies Box",
                        quantity = 2.0,
                        unit = "box",
                        priceAtOrder = 12.00
                    )
                ),
                totalPrice = 24.00,
                notes = "Gift wrap please.",
                status = "PENDING"
            )

            // Insert orders
            repository.insertOrder(order1)
            repository.insertOrder(order2)
            repository.insertOrder(order3)
        }
    }

    // Inventory operations
    fun addInventoryItem(name: String, category: String, quantity: Double, unit: String, price: Double, minThreshold: Double) {
        viewModelScope.launch {
            val item = InventoryItem(
                name = name.trim(),
                category = category,
                quantity = quantity,
                unit = unit,
                price = price,
                minThreshold = minThreshold
            )
            repository.insertInventoryItem(item)
        }
    }

    fun updateInventoryItem(id: Int, name: String, category: String, quantity: Double, unit: String, price: Double, minThreshold: Double) {
        viewModelScope.launch {
            val item = InventoryItem(
                id = id,
                name = name.trim(),
                category = category,
                quantity = quantity,
                unit = unit,
                price = price,
                minThreshold = minThreshold
            )
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteInventoryItemById(id)
        }
    }

    // Order operations
    fun addOrder(customerName: String, customerPhone: String, customerEmail: String, pickupDate: Long, items: List<OrderItem>, totalPrice: Double, notes: String, status: String) {
        viewModelScope.launch {
            val order = CustomerOrder(
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                customerEmail = customerEmail.trim(),
                pickupDate = pickupDate,
                items = items,
                totalPrice = totalPrice,
                notes = notes.trim(),
                status = status
            )
            repository.insertOrder(order)
        }
    }

    fun updateOrder(orderId: Int, customerName: String, customerPhone: String, customerEmail: String, pickupDate: Long, items: List<OrderItem>, totalPrice: Double, notes: String, status: String) {
        viewModelScope.launch {
            val order = CustomerOrder(
                orderId = orderId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                customerEmail = customerEmail.trim(),
                pickupDate = pickupDate,
                items = items,
                totalPrice = totalPrice,
                notes = notes.trim(),
                status = status
            )
            repository.updateOrder(order)
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrderById(orderId)
        }
    }
}
