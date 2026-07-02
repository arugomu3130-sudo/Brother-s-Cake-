package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class OrderItem(
    val itemId: Int,          // Linked inventory item ID
    val name: String,         // Item name snapshot
    val quantity: Double,     // Ordered quantity
    val unit: String,         // Item unit (pcs, kg, etc.)
    val priceAtOrder: Double  // Price snapshot
)

@Entity(tableName = "customer_orders")
data class CustomerOrder(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val pickupDate: Long,     // Delivery / pickup timestamp
    val items: List<OrderItem>,
    val totalPrice: Double,
    val notes: String,
    val status: String,       // PENDING, IN_PROGRESS, READY, DELIVERED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)
