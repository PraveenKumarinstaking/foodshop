package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordString: String, // Plain-text for demo simplicity
    val role: String, // "Customer", "ShopOwner", "Deliverer", "Admin"
    val fullName: String,
    val phone: String,
    val address: String
)

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: Int, // Refers to User.id of role ShopOwner
    val name: String,
    val cuisineType: String,
    val address: String,
    val rating: Double = 4.5,
    val imageUrlIndex: Int = 0, // Code-level reference to a local food image/style index
    val isApproved: Boolean = false // Administrative approval
)

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shopId: Int, // Refers to Shop.id
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Burger", "Pizza", "Salads", "Drinks", "Desserts"
    val isAvailable: Boolean = true
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int, // Refers to User.id of role Customer
    val shopId: Int, // Refers to Shop.id
    val delivererId: Int? = null, // Refers to User.id of role Deliverer (null until claimed)
    val status: String, // "Pending", "Preparing", "ReadyForPickup", "OutForDelivery", "Delivered", "Cancelled"
    val totalPrice: Double,
    val customerAddress: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deliveryProgress: Float = 0.0f // 0.0 to 1.0 as deliverer progresses on route
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int, // Refers to Order.id
    val menuItemId: Int,
    val itemName: String,
    val itemPrice: Double,
    val quantity: Int
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // Refers to User.id (recipient)
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
