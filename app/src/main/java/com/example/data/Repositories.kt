package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodDeliveryRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val shopDao = db.shopDao()
    private val menuItemDao = db.menuItemDao()
    private val orderDao = db.orderDao()
    private val notificationDao = db.notificationDao()

    // --- USER / AUTH FUNCTIONS ---
    suspend fun authenticateUser(username: String, passwordString: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username.trim())
        if (user != null && user.passwordString == passwordString) {
            user
        } else {
            null
        }
    }

    suspend fun registerUser(user: User): Long = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByUsername(user.username.trim())
        if (existing != null) {
            -1L // Name conflict
        } else {
            userDao.insertUser(user)
        }
    }

    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id).flowOn(Dispatchers.IO)
    suspend fun getUserByIdSync(id: Int): User? = userDao.getUserByIdSync(id)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllAsFlow().flowOn(Dispatchers.IO)

    // --- SHOP FUNCTIONS ---
    fun getApprovedShops(): Flow<List<Shop>> = shopDao.getApprovedShops().flowOn(Dispatchers.IO)
    fun getAllShops(): Flow<List<Shop>> = shopDao.getAllShops().flowOn(Dispatchers.IO)
    fun getShopsByOwner(ownerId: Int): Flow<List<Shop>> = shopDao.getShopsByOwner(ownerId).flowOn(Dispatchers.IO)
    fun getShopById(id: Int): Flow<Shop?> = shopDao.getShopById(id).flowOn(Dispatchers.IO)
    suspend fun getShopByIdSync(id: Int): Shop? = shopDao.getShopByIdSync(id)
    
    suspend fun createShop(shop: Shop): Long = withContext(Dispatchers.IO) {
        shopDao.insertShop(shop)
    }

    suspend fun updateShop(shop: Shop) = withContext(Dispatchers.IO) {
        shopDao.updateShop(shop)
    }

    suspend fun approveShop(shopId: Int) = withContext(Dispatchers.IO) {
        shopDao.approveShop(shopId)
        // Send a notification to the owner of this shop informing them of approval.
        val shop = shopDao.getShopByIdSync(shopId)
        if (shop != null) {
            notificationDao.insertNotification(
                Notification(
                    userId = shop.ownerId,
                    title = "Shop Approved! 🎉",
                    message = "Your shop \"${shop.name}\" has been verified and is now live for customers."
                )
            )
        }
    }

    // --- MENU ITEM FUNCTIONS ---
    fun getAvailableItemsByShop(shopId: Int): Flow<List<MenuItem>> = menuItemDao.getAvailableItemsByShop(shopId).flowOn(Dispatchers.IO)
    fun getAllItemsByShop(shopId: Int): Flow<List<MenuItem>> = menuItemDao.getAllItemsByShop(shopId).flowOn(Dispatchers.IO)
    
    suspend fun insertMenuItem(item: MenuItem): Long = withContext(Dispatchers.IO) {
        menuItemDao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.updateMenuItem(item)
    }

    suspend fun deleteMenuItem(id: Int) = withContext(Dispatchers.IO) {
        menuItemDao.deleteMenuItemById(id)
    }

    // --- ORDER FUNCTIONS ---
    fun getOrdersForCustomer(customerId: Int): Flow<List<Order>> = orderDao.getOrdersForCustomer(customerId).flowOn(Dispatchers.IO)
    fun getOrdersForShop(shopId: Int): Flow<List<Order>> = orderDao.getOrdersForShop(shopId).flowOn(Dispatchers.IO)
    fun getOrdersForDeliverer(delivererId: Int): Flow<List<Order>> = orderDao.getOrdersForDeliverer(delivererId).flowOn(Dispatchers.IO)
    fun getAvailableOrdersForPickup(): Flow<List<Order>> = orderDao.getAvailableOrdersForPickup().flowOn(Dispatchers.IO)
    fun getOrderById(id: Int): Flow<Order?> = orderDao.getOrderById(id).flowOn(Dispatchers.IO)
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders().flowOn(Dispatchers.IO)

    suspend fun getOrderItemsSync(orderId: Int): List<OrderItem> = orderDao.getOrderItemsSync(orderId)
    fun getOrderItems(orderId: Int): Flow<List<OrderItem>> = orderDao.getOrderItems(orderId).flowOn(Dispatchers.IO)

    suspend fun placeOrder(order: Order, items: List<Pair<MenuItem, Int>>): Long = withContext(Dispatchers.IO) {
        val orderId = orderDao.insertOrder(order)
        for (pair in items) {
            val details = OrderItem(
                orderId = orderId.toInt(),
                menuItemId = pair.first.id,
                itemName = pair.first.name,
                itemPrice = pair.first.price,
                quantity = pair.second
            )
            orderDao.insertOrderItem(details)
        }

        // Send confirmation notify to customer
        notificationDao.insertNotification(
            Notification(
                userId = order.customerId,
                title = "Order Created #$orderId",
                message = "Your order has been placed with total $${String.format("%.2f", order.totalPrice)}. Waiting for shop approval."
            )
        )

        // Send notify to restaurant owner
        val shop = shopDao.getShopByIdSync(order.shopId)
        if (shop != null) {
            notificationDao.insertNotification(
                Notification(
                    userId = shop.ownerId,
                    title = "New Order Recieved! 🍔",
                    message = "Order #$orderId received for \"${shop.name}\" worth $${String.format("%.2f", order.totalPrice)}."
                )
            )
        }

        orderId
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String, delivererId: Int? = null, progress: Float? = null) = withContext(Dispatchers.IO) {
        val existing = orderDao.getOrderByIdSync(orderId)
        if (existing != null) {
            val updated = existing.copy(
                status = newStatus,
                delivererId = delivererId ?: existing.delivererId,
                deliveryProgress = progress ?: existing.deliveryProgress,
                updatedAt = System.currentTimeMillis()
            )
            orderDao.updateOrder(updated)

            // Trigger notification based on status transition
            val customerId = existing.customerId
            val shop = shopDao.getShopByIdSync(existing.shopId)
            val merchantId = shop?.ownerId

            val statusText = when (newStatus) {
                "Preparing" -> "is now being Prepared by the kitchen!"
                "ReadyForPickup" -> "is Ready for Pickup!"
                "OutForDelivery" -> "is Out for Delivery with your rider!"
                "Delivered" -> "has been successfully Delivered! Enjoy your meal! 🎉"
                "Cancelled" -> "has been Cancelled."
                else -> "status updated to $newStatus."
            }

            // Notify Customer
            notificationDao.insertNotification(
                Notification(
                    userId = customerId,
                    title = "Order #$orderId Update",
                    message = "Your order from \"${shop?.name ?: "Shop"}\" $statusText"
                )
            )

            // Notify Merchant if Rider grabs it or delivers
            if (merchantId != null && (newStatus == "OutForDelivery" || newStatus == "Delivered")) {
                val riderText = if (newStatus == "OutForDelivery") "picked up by rider" else "delivered to customer"
                notificationDao.insertNotification(
                    Notification(
                        userId = merchantId,
                        title = "Order #$orderId Handled",
                        message = "Order #$orderId is $riderText by driver."
                    )
                )
            }

            // Notify Deliverer if Status changed externally (not usually applicable but nice)
            if (delivererId != null) {
                notificationDao.insertNotification(
                    Notification(
                        userId = delivererId,
                        title = "Delivery Status",
                        message = "You updated Order #$orderId to $newStatus"
                    )
                )
            }
        }
    }

    // --- NOTIFICATION FUNCTIONS ---
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>> = notificationDao.getNotificationsForUser(userId).flowOn(Dispatchers.IO)

    suspend fun insertNotification(userId: Int, title: String, message: String) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(Notification(userId = userId, title = title, message = message))
    }

    suspend fun markAllNotificationsAsRead(userId: Int) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun deleteNotification(id: Int) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }
}
