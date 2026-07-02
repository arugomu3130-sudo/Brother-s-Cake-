package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Cakes, Pastries, Bread, Cookies, Custom
    val quantity: Double,
    val unit: String,     // pcs, kg, g, box
    val price: Double,
    val minThreshold: Double,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= minThreshold
}
