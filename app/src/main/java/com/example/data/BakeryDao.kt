package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BakeryDao {
    // Inventory Queries
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: Int): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItemById(id: Int)

    @Query("UPDATE inventory_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateInventoryItemQuantity(id: Int, quantity: Double)

    // Customer Order Queries
    @Query("SELECT * FROM customer_orders ORDER BY pickupDate ASC")
    fun getAllOrders(): Flow<List<CustomerOrder>>

    @Query("SELECT * FROM customer_orders WHERE orderId = :id LIMIT 1")
    suspend fun getOrderById(id: Int): CustomerOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CustomerOrder): Long

    @Update
    suspend fun updateOrder(order: CustomerOrder)

    @Query("DELETE FROM customer_orders WHERE orderId = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("UPDATE customer_orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)
}
