package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerOrder
import com.example.data.InventoryItem
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BakeryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BakeryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: BakeryViewModel) {
    val inventory by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("Dashboard") }
    
    // Detailed expanded order trigger from dashboard
    var initialExpandedOrderId by remember { mutableStateOf<Int?>(null) }

    // Dialog state
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var editingInventoryItem by remember { mutableStateOf<InventoryItem?>(null) }
    
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<CustomerOrder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            "Dashboard" -> "SweetTrack Dashboard"
                            "Inventory" -> "Bakery Inventory"
                            else -> "Customer Orders"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentTab == "Dashboard",
                    onClick = { currentTab = "Dashboard" },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = currentTab == "Inventory",
                    onClick = { currentTab = "Inventory" },
                    modifier = Modifier.testTag("nav_tab_inventory")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = currentTab == "Orders",
                    onClick = { currentTab = "Orders" },
                    modifier = Modifier.testTag("nav_tab_orders")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "Dashboard" -> {
                    DashboardScreen(
                        inventory = inventory,
                        orders = orders,
                        onNavigateToInventory = { currentTab = "Inventory" },
                        onNavigateToOrders = { currentTab = "Orders" },
                        onRestockItem = { item ->
                            editingInventoryItem = item
                        },
                        onViewOrder = { order ->
                            initialExpandedOrderId = order.orderId
                            currentTab = "Orders"
                        }
                    )
                }
                "Inventory" -> {
                    InventoryScreen(
                        inventory = inventory,
                        onAddItem = { showAddInventoryDialog = true },
                        onEditItem = { item -> editingInventoryItem = item },
                        onDeleteItem = { item -> viewModel.deleteInventoryItem(item.id) }
                    )
                }
                "Orders" -> {
                    OrdersScreen(
                        orders = orders,
                        onAddOrder = { showAddOrderDialog = true },
                        onEditOrder = { order -> editingOrder = order },
                        onDeleteOrder = { order -> viewModel.deleteOrder(order.orderId) },
                        onUpdateStatus = { orderId, status -> viewModel.updateOrderStatus(orderId, status) },
                        initialExpandedOrderId = initialExpandedOrderId
                    )
                    // Reset single expandable selection once loaded
                    LaunchedEffect(currentTab) {
                        initialExpandedOrderId = null
                    }
                }
            }
        }

        // Add Inventory Item Dialog
        if (showAddInventoryDialog) {
            AddEditInventoryDialog(
                onDismiss = { showAddInventoryDialog = false },
                onSave = { name, category, quantity, unit, price, minThreshold ->
                    viewModel.addInventoryItem(name, category, quantity, unit, price, minThreshold)
                    showAddInventoryDialog = false
                }
            )
        }

        // Edit Inventory Item Dialog
        if (editingInventoryItem != null) {
            AddEditInventoryDialog(
                item = editingInventoryItem,
                onDismiss = { editingInventoryItem = null },
                onSave = { name, category, quantity, unit, price, minThreshold ->
                    viewModel.updateInventoryItem(editingInventoryItem!!.id, name, category, quantity, unit, price, minThreshold)
                    editingInventoryItem = null
                }
            )
        }

        // Add Customer Order Dialog
        if (showAddOrderDialog) {
            AddEditOrderDialog(
                availableInventory = inventory,
                onDismiss = { showAddOrderDialog = false },
                onSave = { name, phone, email, date, items, total, notes, status ->
                    viewModel.addOrder(name, phone, email, date, items, total, notes, status)
                    showAddOrderDialog = false
                }
            )
        }

        // Edit Customer Order Dialog
        if (editingOrder != null) {
            AddEditOrderDialog(
                order = editingOrder,
                availableInventory = inventory,
                onDismiss = { editingOrder = null },
                onSave = { name, phone, email, date, items, total, notes, status ->
                    viewModel.updateOrder(editingOrder!!.orderId, name, phone, email, date, items, total, notes, status)
                    editingOrder = null
                }
            )
        }
    }
}
