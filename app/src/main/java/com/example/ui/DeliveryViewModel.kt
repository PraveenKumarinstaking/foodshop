package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodDeliveryRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodDeliveryRepository(database)
    }

    // --- AUTH STATE ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- SEED USER SWITCHING STATE (for easy showcase testing) ---
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- GENERAL SHOT DATA ---
    val approvedShops: StateFlow<List<Shop>> = repository.getApprovedShops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShops: StateFlow<List<Shop>> = repository.getAllShops()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SELECTED RESTAURANT & MENU STATE ---
    private val _selectedShop = MutableStateFlow<Shop?>(null)
    val selectedShop: StateFlow<Shop?> = _selectedShop.asStateFlow()

    val currentShopMenuItems: StateFlow<List<MenuItem>> = _selectedShop
        .flatMapLatest { shop ->
            if (shop != null) {
                repository.getAvailableItemsByShop(shop.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CUSTOMER CART STATE ---
    private val _cartShop = MutableStateFlow<Shop?>(null)
    val cartShop: StateFlow<Shop?> = _cartShop.asStateFlow()

    private val _cartItems = MutableStateFlow<Map<MenuItem, Int>>(emptyMap())
    val cartItems: StateFlow<Map<MenuItem, Int>> = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.entries.sumOf { it.key.price * it.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- NOTIFICATIONS STATE ---
    val userNotifications: StateFlow<List<Notification>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getNotificationsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ORDERS STREAMS BASED ON ROLE ---
    val customerOrders: StateFlow<List<Order>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "Customer") {
                repository.getOrdersForCustomer(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myMerchantShops: StateFlow<List<Shop>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "ShopOwner") {
                repository.getShopsByOwner(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders for merchant shops
    val merchantOrders: StateFlow<List<Order>> = myMerchantShops
        .flatMapLatest { shops ->
            if (shops.isNotEmpty()) {
                // Combine orders from all owned shops
                val flows = shops.map { repository.getOrdersForShop(it.id) }
                combine(flows) { ordersList ->
                    ordersList.flatMap { it }.sortedByDescending { it.createdAt }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val delivererOrders: StateFlow<List<Order>> = _currentUser
        .flatMapLatest { user ->
            if (user != null && user.role == "Deliverer") {
                repository.getOrdersForDeliverer(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableOrdersForPickup: StateFlow<List<Order>> = repository.getAvailableOrdersForPickup()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrdersForAdmin: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active tracking progress simulation
    private val activeSimulations = mutableMapOf<Int, Job>()

    // --- ACTIONS: AUTHENTICATION ---
    fun login(username: String, passwordString: String) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.authenticateUser(username, passwordString)
            if (user != null) {
                _currentUser.value = user
            } else {
                _authError.value = "Invalid username or password"
            }
        }
    }

    fun loginAsUser(user: User) {
        viewModelScope.launch {
            _authError.value = null
            _currentUser.value = user
        }
    }

    fun register(username: String, passwordString: String, role: String, fullName: String, phone: String, address: String) {
        viewModelScope.launch {
            _authError.value = null
            if (username.isBlank() || passwordString.isBlank() || fullName.isBlank() || phone.isBlank()) {
                _authError.value = "Please fill in all required fields"
                return@launch
            }
            val newUser = User(
                username = username.trim(),
                passwordString = passwordString,
                role = role,
                fullName = fullName,
                phone = phone,
                address = address
            )
            val result = repository.registerUser(newUser)
            if (result == -1L) {
                _authError.value = "Username already exists!"
            } else {
                // Auto login on success
                val createdUser = newUser.copy(id = result.toInt())
                _currentUser.value = createdUser
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _cartItems.value = emptyMap()
        _cartShop.value = null
        _selectedShop.value = null
        _authError.value = null
    }

    // --- ACTIONS: CART MANAGEMENT ---
    fun selectShop(shop: Shop?) {
        _selectedShop.value = shop
    }

    fun addCartItem(item: MenuItem, shop: Shop) {
        val currentCartShop = _cartShop.value
        if (currentCartShop != null && currentCartShop.id != shop.id) {
            // Cannot order from multiple shops at once. Clear old cart.
            _cartItems.value = mapOf(item to 1)
            _cartShop.value = shop
        } else {
            if (currentCartShop == null) {
                _cartShop.value = shop
            }
            val currentMap = _cartItems.value.toMutableMap()
            val currentQty = currentMap[item] ?: 0
            currentMap[item] = currentQty + 1
            _cartItems.value = currentMap
        }
    }

    fun removeCartItem(item: MenuItem) {
        val currentMap = _cartItems.value.toMutableMap()
        val currentQty = currentMap[item] ?: 0
        if (currentQty <= 1) {
            currentMap.remove(item)
        } else {
            currentMap[item] = currentQty - 1
        }
        _cartItems.value = currentMap
        if (currentMap.isEmpty()) {
            _cartShop.value = null
        }
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _cartShop.value = null
    }

    fun createOrder(specialAddress: String? = null) {
        val user = _currentUser.value ?: return
        val shop = _cartShop.value ?: return
        val itemsList = _cartItems.value.toList()
        if (itemsList.isEmpty()) return

        val total = itemsList.sumOf { it.first.price * it.second } + 20.00
        val address = if (!specialAddress.isNullOrBlank()) specialAddress else user.address

        viewModelScope.launch {
            val order = Order(
                customerId = user.id,
                shopId = shop.id,
                delivererId = null,
                status = "Pending",
                totalPrice = total,
                customerAddress = address
            )
            repository.placeOrder(order, itemsList)
            clearCart()
        }
    }

    // --- ACTIONS: ORDER OPERATIONS ---
    fun acceptOrderMerchant(orderId: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "Preparing")
        }
    }

    fun readyForPickupMerchant(orderId: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "ReadyForPickup")
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "Cancelled")
        }
    }

    fun claimDelivery(orderId: Int) {
        val deliverer = _currentUser.value ?: return
        if (deliverer.role != "Deliverer") return
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "Preparing", delivererId = deliverer.id) // Driver claims it
            // Immediately mark it out for delivery if already ready, or keep it. Let's make it go Preparing -> ReadyForPickup -> OutForDelivery.
            // If the driver claims it, let's update status to Preparing with delivererId or let them transition to delivery.
            repository.updateOrderStatus(orderId, "OutForDelivery", delivererId = deliverer.id, progress = 0.0f)
            // Kick off simulated tracking coroutine!
            launchSimulatedRider(orderId, deliverer.id)
        }
    }

    // --- SIMULATION OF REAL-TIME DELIVERY ---
    private fun launchSimulatedRider(orderId: Int, driverId: Int) {
        // Cancel active job if exists
        activeSimulations[orderId]?.cancel()

        activeSimulations[orderId] = viewModelScope.launch {
            var progress = 0.0f
            while (progress < 1.0f) {
                delay(3000) // Advances every 3s
                progress += 0.2f
                if (progress >= 1.0f) progress = 1.0f
                
                repository.updateOrderStatus(orderId, "OutForDelivery", delivererId = driverId, progress = progress)
                
                if (progress >= 1.0f) {
                    repository.updateOrderStatus(orderId, "Delivered", delivererId = driverId, progress = 1.0f)
                    break
                }
            }
            activeSimulations.remove(orderId)
        }
    }

    fun getOrderItems(orderId: Int): Flow<List<OrderItem>> {
        return repository.getOrderItems(orderId)
    }

    // --- ACTIONS: MERCHANT SHOP CREATION / MENU MANAGEMENT ---
    fun registerShop(name: String, cuisineType: String, address: String, styleIndex: Int) {
        val owner = _currentUser.value ?: return
        if (owner.role != "ShopOwner") return
        viewModelScope.launch {
            val newShop = Shop(
                ownerId = owner.id,
                name = name,
                cuisineType = cuisineType,
                address = address,
                imageUrlIndex = styleIndex,
                isApproved = false // Requires Admin approval
            )
            val result = repository.createShop(newShop)
            // Add initial items for the new shop so it isn't empty!
            repository.insertMenuItem(MenuItem(shopId = result.toInt(), name = "Chef's Signature Burger", description = "Freshly grilled wagyu, double Colby cheese, and signature tomato chutney.", price = 15.00, category = "Burger"))
            repository.insertMenuItem(MenuItem(shopId = result.toInt(), name = "Hand-Cut Sea Salt Fries", description = "Basket of crispy fresh chips seasoned with coarse grey sea salt.", price = 4.50, category = "Appetizer"))
            
            // Notify admin about registration
            val admins = allUsers.value.filter { it.role == "Admin" }
            for (admin in admins) {
                repository.insertNotification(
                    userId = admin.id,
                    title = "Pending Shop Approval",
                    message = "Merchant \"${owner.fullName}\" submitted \"$name\" for administrative review."
                )
            }
        }
    }

    fun addMenuItem(shopId: Int, name: String, description: String, price: Double, category: String) {
        viewModelScope.launch {
            val item = MenuItem(
                shopId = shopId,
                name = name,
                description = description,
                price = price,
                category = category
            )
            repository.insertMenuItem(item)
        }
    }

    fun deleteMenuItemAndRefresh(id: Int) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
        }
    }

    // --- ACTIONS: ADMINISTRATIVE STUFF ---
    fun approveShopAndApprove(shopId: Int) {
        viewModelScope.launch {
            repository.approveShop(shopId)
        }
    }

    // --- ACTIONS: NOTIFICATIONS MANAGEMENT ---
    fun clearNotifications() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeSimulations.values.forEach { it.cancel() }
        activeSimulations.clear()
    }
}
