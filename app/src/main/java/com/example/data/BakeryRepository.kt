package com.example.data

import kotlinx.coroutines.flow.Flow

class BakeryRepository(private val bakeryDao: BakeryDao) {
    val allInventoryItems: Flow<List<InventoryItem>> = bakeryDao.getAllInventoryItems()
    val allOrders: Flow<List<CustomerOrder>> = bakeryDao.getAllOrders()

    suspend fun getInventoryItemById(id: Int): InventoryItem? {
        return bakeryDao.getInventoryItemById(id)
    }

    suspend fun insertInventoryItem(item: InventoryItem): Long {
        return bakeryDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        bakeryDao.updateInventoryItem(item)
    }

    suspend fun deleteInventoryItemById(id: Int) {
        bakeryDao.deleteInventoryItemById(id)
    }

    suspend fun insertOrder(order: CustomerOrder): Long {
        val id = bakeryDao.insertOrder(order)
        // Deduct inventory items stock for non-cancelled orders
        if (order.status != "CANCELLED") {
            for (orderItem in order.items) {
                if (orderItem.itemId > 0) {
                    deductStock(orderItem.itemId, orderItem.quantity)
                }
            }
        }
        return id
    }

    suspend fun updateOrder(order: CustomerOrder) {
        val oldOrder = bakeryDao.getOrderById(order.orderId)
        bakeryDao.updateOrder(order)

        if (oldOrder != null) {
            // If previous order was not cancelled, restore its old stock first
            if (oldOrder.status != "CANCELLED") {
                for (item in oldOrder.items) {
                    if (item.itemId > 0) {
                        restoreStock(item.itemId, item.quantity)
                    }
                }
            }
            // Now apply new stock deduction if the new order status is not cancelled
            if (order.status != "CANCELLED") {
                for (item in order.items) {
                    if (item.itemId > 0) {
                        deductStock(item.itemId, item.quantity)
                    }
                }
            }
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String) {
        val oldOrder = bakeryDao.getOrderById(orderId) ?: return
        val oldStatus = oldOrder.status
        bakeryDao.updateOrderStatus(orderId, newStatus)

        // Stock behavior logic:
        if (oldStatus != "CANCELLED" && newStatus == "CANCELLED") {
            // Restore stock
            for (orderItem in oldOrder.items) {
                if (orderItem.itemId > 0) {
                    restoreStock(orderItem.itemId, orderItem.quantity)
                }
            }
        } else if (oldStatus == "CANCELLED" && newStatus != "CANCELLED") {
            // Re-deduct stock
            for (orderItem in oldOrder.items) {
                if (orderItem.itemId > 0) {
                    deductStock(orderItem.itemId, orderItem.quantity)
                }
            }
        }
    }

    suspend fun deleteOrderById(orderId: Int) {
        val oldOrder = bakeryDao.getOrderById(orderId)
        bakeryDao.deleteOrderById(orderId)

        // If deleting an order that was NOT cancelled, restore the stock!
        if (oldOrder != null && oldOrder.status != "CANCELLED") {
            for (orderItem in oldOrder.items) {
                if (orderItem.itemId > 0) {
                    restoreStock(orderItem.itemId, orderItem.quantity)
                }
            }
        }
    }

    private suspend fun deductStock(itemId: Int, quantity: Double) {
        val item = bakeryDao.getInventoryItemById(itemId) ?: return
        val newQty = (item.quantity - quantity).coerceAtLeast(0.0)
        bakeryDao.updateInventoryItemQuantity(itemId, newQty)
    }

    private suspend fun restoreStock(itemId: Int, quantity: Double) {
        val item = bakeryDao.getInventoryItemById(itemId) ?: return
        val newQty = item.quantity + quantity
        bakeryDao.updateInventoryItemQuantity(itemId, newQty)
    }
}
