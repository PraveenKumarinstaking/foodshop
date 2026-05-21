package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(viewModel: DeliveryViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showRegisterForm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val user = currentUser
            if (user == null) {
                // Not authenticated
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    if (showRegisterForm) {
                        RegisterScreen(
                            onRegister = { username, password, role, name, phone, address ->
                                viewModel.register(username, password, role, name, phone, address)
                            },
                            onBackToLogin = {
                                showRegisterForm = false
                            },
                            errorMessage = authError
                        )
                    } else {
                        LoginScreen(
                            onLogin = { username, password ->
                                viewModel.login(username, password)
                            },
                            onGoToRegister = {
                                showRegisterForm = true
                            },
                            errorMessage = authError,
                            users = allUsers,
                            onQuickLogin = { quickUser ->
                                viewModel.loginAsUser(quickUser)
                            }
                        )
                    }
                }
            } else {
                // Authenticated workspace
                WorkspaceContainer(user = user, viewModel = viewModel)
            }
        }
    }
}

// --- AUTHENTICATION SCREENS ---
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onGoToRegister: () -> Unit,
    errorMessage: String?,
    users: List<User>,
    onQuickLogin: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier
                    .size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BiteSpeed Namakkal",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Namakkal's High-Speed Food & Poultry Delivery Network",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Info Card about Namakkal, Tamil Nadu & Demo Access
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Localized for Namakkal, TN, India",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Experience poultry specialties, authentic farm-fresh egg delicacies, and Kongunadu flavors of Mohanur Road & Salem Road. Active order simulation is fully active!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🔓 DEMO USER ACCESS ENABLED FOR ALL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Simply tap any profile in the Quick Demo Login section below to jump in as Customer, Restaurant Owner, Delivery Rider, or Platform Admin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Standard Login Inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Access Platform",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onLogin(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("New to BiteSpeed?", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onGoToRegister) {
                            Text("Register Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QUICK DEMO ACCESS PANEL (Highly useful for fast testing of the 4 roles!)
            Text(
                text = "⚡ QUICK DEMO LOGIN (ONE-TAP)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // List pre-seeded accounts for easy testing
        items(users) { user ->
            val roleIcon = when (user.role) {
                "Admin" -> Icons.Filled.AdminPanelSettings
                "Customer" -> Icons.Filled.ShoppingCart
                "ShopOwner" -> Icons.Filled.Kitchen
                else -> Icons.Filled.DeliveryDining
            }
            val roleColor = when (user.role) {
                "Admin" -> Color(0xFFC62828)
                "Customer" -> Color(0xFF1565C0)
                "ShopOwner" -> Color(0xFF2E7D32)
                else -> Color(0xFFEF6C00)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onQuickLogin(user) }
                    .testTag("quick_login_${user.username}"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, roleColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(roleColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = roleIcon, contentDescription = null, tint = roleColor)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Login as ${user.role} (u: ${user.username})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String, String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    errorMessage: String?
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Customer") } // Customer, ShopOwner, Deliverer

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Join BiteSpeed",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Choose your role & register below",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Account Role Set",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Role selection tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(4.dp)
                    ) {
                        val rolesList = listOf("Customer", "ShopOwner", "Deliverer")
                        rolesList.forEach { role ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (role) {
                                        "ShopOwner" -> "Merchant"
                                        "Deliverer" -> "Rider"
                                        else -> "Customer"
                                    },
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Desired Username *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password")
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_fullname")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery/Business Address *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_address")
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onRegister(username, password, selectedRole, fullName, phone, address)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_registration"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish Registration", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Back to Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- MASTER WORKSPACE WORKSPACE CONTAINER ---
@Composable
fun WorkspaceContainer(user: User, viewModel: DeliveryViewModel) {
    var showNotifDrawer by remember { mutableStateOf(false) }
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val unreadNotifCount = notifications.size

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Platform Global TopBar Header
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Identity
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BiteSpeed • " + when (user.role) {
                            "Admin" -> "ADMIN PANEL"
                            "ShopOwner" -> "MERCHANT"
                            "Deliverer" -> "COURIER"
                            else -> "CUSTOMER"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hi, ${user.fullName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Interaction tools
                // In-App Real-time notifications alert bell icon
                IconButton(
                    onClick = { showNotifDrawer = true },
                    modifier = Modifier.testTag("notif_bell_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(unreadNotifCount.toString(), color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (unreadNotifCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                            contentDescription = "Notifications list"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Logout account",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Visual divider line with animated heartbeat color glowing indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
        )

        // Primary content workspace based on active user's roles
        Box(modifier = Modifier.weight(1f)) {
            when (user.role) {
                "Admin" -> AdminWorkspace(viewModel = viewModel)
                "ShopOwner" -> MerchantWorkspace(viewModel = viewModel)
                "Deliverer" -> CourierWorkspace(viewModel = viewModel)
                else -> CustomerWorkspace(viewModel = viewModel)
            }

            // Real-time notification sliding sheet
            if (showNotifDrawer) {
                DialogNotifDrawer(
                    notifications = notifications,
                    onDismiss = { showNotifDrawer = false },
                    onClearAll = {
                        viewModel.clearNotifications()
                    }
                )
            }
        }
    }
}

// --- WORKSPACE LAYOUT: CUSTOMER ---
@Composable
fun CustomerWorkspace(viewModel: DeliveryViewModel) {
    val shops by viewModel.approvedShops.collectAsStateWithLifecycle()
    val selectedShop by viewModel.selectedShop.collectAsStateWithLifecycle()
    val menuItems by viewModel.currentShopMenuItems.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val activeOrders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val cartShop by viewModel.cartShop.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Shops") } // "Shops", "Cart", "Track"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // M3 Bottom Navigation Rail replacement with Standard Bottom navigation for ergonomic thumbs
            Column(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    "Shops" -> {
                        if (selectedShop != null) {
                            // Browsing a specific shop
                            ShopDetailView(
                                shop = selectedShop!!,
                                menuItems = menuItems,
                                cartItems = cartItems,
                                onBack = { viewModel.selectShop(null) },
                                onAdd = { item -> viewModel.addCartItem(item, selectedShop!!) },
                                onRemove = { item -> viewModel.removeCartItem(item) },
                                onCloseSelection = { viewModel.selectShop(null) }
                            )
                        } else {
                            // Browsing shop index
                            CustomerShopList(
                                shops = shops,
                                onSelectShop = { shop -> viewModel.selectShop(shop) }
                            )
                        }
                    }
                    "Cart" -> {
                        CustomerCartView(
                            cartShop = cartShop,
                            cartItems = cartItems,
                            cartTotal = cartTotal,
                            onAdd = { item, shop -> viewModel.addCartItem(item, shop) },
                            onRemove = { item -> viewModel.removeCartItem(item) },
                            onClear = { viewModel.clearCart() },
                            onPlaceOrder = { customAddress ->
                                viewModel.createOrder(customAddress)
                                activeTab = "Track" // Switch immediately to track layout
                            }
                        )
                    }
                    "Track" -> {
                        CustomerOrdersTrackerView(
                            activeOrders = activeOrders,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Custom M3 pill navigation style
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "Shops",
                    onClick = {
                        activeTab = "Shops"
                        // If selecting shop, reset selectedShop if we tab back to avoid getting stuck!
                        // No, actually keep it, but let them click again or reset
                    },
                    icon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                    label = { Text("Restaurants") }
                )
                NavigationBarItem(
                    selected = activeTab == "Cart",
                    onClick = { activeTab = "Cart" },
                    icon = {
                        BadgedBox(
                            badge = {
                                val size = cartItems.values.sum()
                                if (size > 0) {
                                    Badge { Text(size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                        }
                    },
                    label = { Text("My Cart") }
                )
                NavigationBarItem(
                    selected = activeTab == "Track",
                    onClick = { activeTab = "Track" },
                    icon = {
                        BadgedBox(badge = {
                            val count = activeOrders.count { it.status != "Delivered" && it.status != "Cancelled" }
                            if (count > 0) {
                                Badge { Text(count.toString()) }
                            }
                        }) {
                            Icon(Icons.Filled.DirectionsBike, contentDescription = null)
                        }
                    },
                    label = { Text("Tracking") }
                )
            }
        }

        // Subtly float a Cart bar overlay if active items exist & browsing shops to prompt checkout
        val activeCartSize = cartItems.values.sum()
        if (activeTab == "Shops" && activeCartSize > 0 && selectedShop != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .clickable { activeTab = "Cart" },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("$activeCartSize items in basket", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("From " + (cartShop?.name ?: ""), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹" + String.format("%.2f", cartTotal), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerShopList(
    shops: List<Shop>,
    onSelectShop: (Shop) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Special Offers Around You 🍕",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fast local deliveries curated fresh everyday",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (shops.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No registered/approved shops available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Create one using a Merchant account, or check Admin!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        items(shops) { shop ->
            // High-fidelity curated food cards
            val themeBrush = when (shop.imageUrlIndex) {
                0 -> Brush.horizontalGradient(listOf(Color(0xFFE64A19), Color(0xFFFF8A65)))
                1 -> Brush.horizontalGradient(listOf(Color(0xFF388E3C), Color(0xFF81C784)))
                else -> Brush.horizontalGradient(listOf(Color(0xFF1976D2), Color(0xFF64B5F6)))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectShop(shop) }
                    .testTag("shop_card_${shop.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column {
                    // Header visual decorative gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .background(themeBrush)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.62f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                               ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(shop.rating.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            // Cuisine badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = shop.cuisineType,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Big beautiful symbol representing the store types
                        Icon(
                            imageVector = when(shop.imageUrlIndex) {
                                0 -> Icons.Filled.Fastfood
                                1 -> Icons.Filled.Kitchen
                                else -> Icons.Filled.Storefront
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(70.dp)
                        )
                    }

                    // Store Info
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = shop.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FmdGood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shop.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopDetailView(
    shop: Shop,
    menuItems: List<MenuItem>,
    cartItems: Map<MenuItem, Int>,
    onBack: () -> Unit,
    onAdd: (MenuItem) -> Unit,
    onRemove: (MenuItem) -> Unit,
    onCloseSelection: () -> Unit
) {
    // Custom wrapper interface because of back button reset
    // To allow user to clear the selected shop in MainUi, we can have a toggle in MainState. Or simpler:
    // Let's create an explicit "Back to General Restaurants" bar that sets SelectedShop back to null!
    val themeBrush = when (shop.imageUrlIndex) {
        0 -> Brush.horizontalGradient(listOf(Color(0xFFE64A19), Color(0xFFFF8A65)))
        1 -> Brush.horizontalGradient(listOf(Color(0xFF388E3C), Color(0xFF81C784)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1976D2), Color(0xFF64B5F6)))
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // Elegant Back toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("shop_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back to restaurants"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back to restaurants list",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBack() }
                )
            }
            
            // Premium banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeBrush)
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                Column {
                    Text("NOW BROWSING", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(shop.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(shop.cuisineType + " • " + shop.address, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header
            Text(
                text = "Premium Menu Dishes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (menuItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No items on this shop menu yet.", fontWeight = FontWeight.Bold)
                        Text("Add items using merchant role!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }

        items(menuItems) { item ->
            // Custom item item card
            val cartQty = cartItems[item] ?: 0

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                border = if (cartQty > 0) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("₹" + String.format("%.2f", item.price), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Quantity interactive picker
                    if (cartQty > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = { onRemove(item) },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Deduct qt", modifier = Modifier.size(16.dp))
                            }
                            Text(cartQty.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            FilledIconButton(
                                onClick = { onAdd(item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add qt", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Button(
                            onClick = { onAdd(item) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("ADD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Action block to pop back to index list
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onBack
                ) {
                    Text("← Back to Restaurants Directory", textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CustomerCartView(
    cartShop: Shop?,
    cartItems: Map<MenuItem, Int>,
    cartTotal: Double,
    onAdd: (MenuItem, Shop) -> Unit,
    onRemove: (MenuItem) -> Unit,
    onClear: () -> Unit,
    onPlaceOrder: (String) -> Unit
) {
    var deliveryAddress by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Shopping Basket 🛒",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Proceed with premium local dish checkout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (cartItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Your shopping cart is empty!", fontWeight = FontWeight.Bold)
                        Text("Add premium meals from the restaurants list", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ordering from: " + (cartShop?.name ?: "Shop"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onClear) {
                        Text("Clear All")
                    }
                }
            }

            items(cartItems.toList()) { (item, qty) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("₹" + String.format("%.2f", item.price) + " each", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(Icons.Filled.Remove, contentDescription = null)
                            }
                            Text(qty.toString(), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onAdd(item, cartShop!!) }) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "₹" + String.format("%.2f", item.price * qty),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Price checkout summaries
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Subtotal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("₹" + String.format("%.2f", cartTotal))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Fast Delivery Fee", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("₹20.00")
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Billing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("₹" + String.format("%.2f", cartTotal + 20.00), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Destination Form
                Text("Fulfill Destination", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address (leave empty to use profile default)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onPlaceOrder(deliveryAddress) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_order_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Place Order (₹" + String.format("%.2f", cartTotal + 20.00) + ")", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CustomerOrdersTrackerView(
    activeOrders: List<Order>,
    viewModel: DeliveryViewModel
) {
    var selectedOrderForMap by remember { mutableStateOf<Order?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Order Tracking & History 🛵",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tap any active delivery to watch the courier in real-time!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Dynamic focused active visual tracker map panel
        val chosenOrder = selectedOrderForMap ?: activeOrders.firstOrNull { it.status != "Delivered" && it.status != "Cancelled" }
        if (chosenOrder != null) {
            item {
                Text(
                    text = "Live Courier Radar (Order #${chosenOrder.id})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                LiveActiveMapWidget(order = chosenOrder, viewModel = viewModel)
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Order Journal Logs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (activeOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No past or progressive orders recorded.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        }

        items(activeOrders) { order ->
            val statusColor = when (order.status) {
                "Pending" -> Color(0xFF1565C0)
                "Preparing" -> Color(0xFF7B1FA2)
                "ReadyForPickup" -> Color(0xFFEF6C00)
                "OutForDelivery" -> Color(0xFFFFB300)
                "Delivered" -> Color(0xFF2E7D32)
                else -> Color(0xFF757575)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedOrderForMap = order },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = if (chosenOrder?.id == order.id) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = order.status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val itemsOrderFlow = remember(order.id) { viewModel.getOrderItems(order.id) }
                    val itemsOrderList by itemsOrderFlow.collectAsStateWithLifecycle(initialValue = emptyList())
                    Text(
                        text = itemsOrderList.joinToString { "${it.quantity}x ${it.itemName}" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Billing: ₹" + String.format("%.2f", order.totalPrice),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap to track", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Custom animated 2D Radar Canvas and Rider simulator track widget
@Composable
fun LiveActiveMapWidget(order: Order, viewModel: DeliveryViewModel) {
    // Dynamic percentage computation
    val rawProgress = order.deliveryProgress
    val progressPercent = (rawProgress * 100).toInt()
    
    // Pulse animation for tracking halo glowing dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Address details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("STATUS PROGRESSION", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = when (order.status) {
                            "Pending" -> "Waiting for kitchen..."
                            "Preparing" -> "Chef is grilling & packing..."
                            "ReadyForPickup" -> "Packaged & sealed for courier!"
                            "OutForDelivery" -> "Courier is riding to you! ($progressPercent%)"
                            "Delivered" -> "Delivered safely! Bon appetit!"
                            else -> "Cancelled"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // If delivering, show live ETA
                if (order.status == "OutForDelivery") {
                    val eta = ((1.0f - rawProgress) * 15).toInt() + 1
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "ETA: $eta MIN",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated map path using Custom Canvas drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            ) {
                // Background grid canvas
                val primColor = MaterialTheme.colorScheme.primary
                val accentColor = MaterialTheme.colorScheme.tertiary
                val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw grid lines
                    val stepsW = 10
                    for (i in 0..stepsW) {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.15f),
                            start = Offset(i * (w / stepsW), 0f),
                            end = Offset(i * (w / stepsW), h),
                            strokeWidth = 1f
                        )
                    }

                    // Draw delivery dashed highway line
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(50f, h / 2f),
                        end = Offset(w - 50f, h / 2f),
                        strokeWidth = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw completed progress highlight line
                    val progressX = 50f + (rawProgress * (w - 100f))
                    if (rawProgress > 0f) {
                        drawLine(
                            color = primColor,
                            start = Offset(50f, h / 2f),
                            end = Offset(progressX, h / 2f),
                            strokeWidth = 6f
                        )
                    }

                    // Shop Node (Kitchen marker)
                    drawCircle(
                        color = accentColor,
                        radius = 12f,
                        center = Offset(50f, h / 2f)
                    )

                    // Customer Node (Home marker)
                    drawCircle(
                        color = primColor,
                        radius = 12f,
                        center = Offset(w - 50f, h / 2f)
                    )
                }

                // Layout icon anchors
                // Shop Icon
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .size(14.dp)
                )

                // Home Icon
                Icon(
                    imageVector = Icons.Filled.FmdGood,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(14.dp)
                )

                // Animated delivery bike dot flowing along progress!
                if (order.status == "OutForDelivery" || order.status == "Delivered") {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val w = maxWidth
                        val paddingOffset = 50.dp
                        val usableWidth = w - paddingOffset * 2
                        val bikeLeft = paddingOffset + (usableWidth * rawProgress) - 18.dp

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = bikeLeft)
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing radar wave halo
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = primColor.copy(alpha = 0.35f * (1f - (pulseRadius / 20f))),
                                    radius = pulseRadius
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = primColor,
                                modifier = Modifier.size(24.dp),
                                tonalElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.DeliveryDining,
                                        contentDescription = "Rider moving",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step instructions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RESTAURANT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("DELIVERY LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pizza Roma / Burger Bistro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(order.customerAddress, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// --- WORKSPACE LAYOUT: MERCHANTS ---
@Composable
fun MerchantWorkspace(viewModel: DeliveryViewModel) {
    val myShops by viewModel.myMerchantShops.collectAsStateWithLifecycle()
    val merchantOrders by viewModel.merchantOrders.collectAsStateWithLifecycle()

    var showAddShopForm by remember { mutableStateOf(false) }
    var shopName by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Merchant Console 👨‍🍳",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Manage your approved food branches, menus and incoming orders here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Shops list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Branches", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Button(
                    onClick = { showAddShopForm = !showAddShopForm },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Register Branch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAddShopForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Register New Branch (Admin Approvals Required)", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Branch / Shop Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_shop_name_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cuisineType,
                            onValueChange = { cuisineType = it },
                            label = { Text("Cuisine Category (e.g. Burgers, Pizza)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_cuisine_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Full Street Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_address_input")
                        )

                        Text("Choose Visual Style Tag", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Tomato", "Basil", "Ocean").forEachIndexed { index, style ->
                                val color = when(index) {
                                    0 -> Color(0xFFE64A19)
                                    1 -> Color(0xFF388E3C)
                                    else -> Color(0xFF1976D2)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedStyle == index) color else color.copy(alpha = 0.2f))
                                        .clickable { selectedStyle = index }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(style, color = if (selectedStyle == index) Color.White else color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (shopName.isNotBlank() && cuisineType.isNotBlank()) {
                                    viewModel.registerShop(shopName, cuisineType, address, selectedStyle)
                                    showAddShopForm = false
                                    shopName = ""
                                    cuisineType = ""
                                    address = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("merchant_submit_shop_btn")
                        ) {
                            Text("Submit to Admin Checklist")
                        }
                    }
                }
            }
        }

        if (myShops.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No branches registered yet.", color = Color.Gray)
                }
            }
        }

        items(myShops) { shop ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (shop.isApproved) Color(0xFF4CAF50) else Color(0xFFFF9800), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(shop.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(shop.cuisineType + " • " + if (shop.isApproved) "Live on Catalog" else "Pending Verification", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    if (!shop.isApproved) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text("Awaiting Admin", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Fulfill business Orders
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Client Orders Fulfillment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (merchantOrders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No orders received yet.", color = Color.Gray)
                }
            }
        }

        items(merchantOrders) { order ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = order.status,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val itemsOrderFlow = remember(order.id) { viewModel.getOrderItems(order.id) }
                    val itemsOrderList by itemsOrderFlow.collectAsStateWithLifecycle(initialValue = emptyList())
                    itemsOrderList.forEach { details ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${details.quantity}x ${details.itemName}", style = MaterialTheme.typography.bodyMedium)
                            Text("₹" + String.format("%.2f", details.itemPrice * details.quantity), color = Color.Gray)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Text("Deliver to: ${order.customerAddress}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Merchant workflow actions: Accept -> Ready
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (order.status) {
                            "Pending" -> {
                                Button(
                                    onClick = { viewModel.acceptOrderMerchant(order.id) },
                                    modifier = Modifier.weight(1f).testTag("accept_order_${order.id}")
                                ) {
                                    Text("Accept Order 🍳", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelOrder(order.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Decline")
                                }
                            }
                            "Preparing" -> {
                                Button(
                                    onClick = { viewModel.readyForPickupMerchant(order.id) },
                                    modifier = Modifier.weight(1f).testTag("ready_order_${order.id}")
                                ) {
                                    Text("Package Complete 📦", fontWeight = FontWeight.Bold)
                                }
                            }
                            "ReadyForPickup" -> {
                                Text("Awaiting Courier Rider pickup...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            "OutForDelivery" -> {
                                val progress = (order.deliveryProgress * 100).toInt()
                                Text("Rider on route to customer ($progress%) 🛵", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            "Delivered" -> {
                                Text("Completed & Dispatched successfully ✅", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- WORKSPACE LAYOUT: COURIER / DELIVERER ---
@Composable
fun CourierWorkspace(viewModel: DeliveryViewModel) {
    val myShipments by viewModel.delivererOrders.collectAsStateWithLifecycle()
    val rawAvailableJobs by viewModel.availableOrdersForPickup.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Courier Rider Hub 🛵",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Accept packages and earn. Toggle route status to trigger simulated navigation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Deliveries assigned
        item {
            Text("Your Active Dispatch Tasks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        val myActiveDeliveries = myShipments.filter { it.status == "OutForDelivery" || it.status == "Preparing" }
        if (myActiveDeliveries.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active delivery runs assigned currently.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }

        items(myActiveDeliveries) { delivery ->
            val progressPercent = (delivery.deliveryProgress * 100).toInt()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delivery Runner #${delivery.id}", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Progress: $progressPercent%",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Destination: " + delivery.customerAddress, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated real-time linear progress indicator
                    LinearProgressIndicator(
                        progress = delivery.deliveryProgress,
                        modifier = Modifier.fillMaxWidth().height(6.onResumeHeight()),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (delivery.status == "OutForDelivery") "Simulated motor scooter driving in progress..." else "Assigned. Waiting for dispatch trigger...",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Browse job board
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Open Package Job Board", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (rawAvailableJobs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No pending food packages ready for pickup currently.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }

        items(rawAvailableJobs) { job ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Job #${job.id}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("From Restaurant Address to customer delivery destination", fontSize = 11.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick-up: Pizza Roma / Burger Bistro", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FmdGood, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Drop-off: " + job.customerAddress, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.claimDelivery(job.id) },
                        modifier = Modifier.fillMaxWidth().testTag("claim_delivery_${job.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept Job & Drive (Start Simulation)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Simple extension helper for layout
private fun Int.onResumeHeight() = 6.dp

// --- WORKSPACE LAYOUT: ADMINISTRATOR ---
@Composable
fun AdminWorkspace(viewModel: DeliveryViewModel) {
    val allShops by viewModel.allShops.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersForAdmin.collectAsStateWithLifecycle()
    val allUsersList by viewModel.allUsers.collectAsStateWithLifecycle()

    val pendingShops = allShops.filter { !it.isApproved }
    val totalRevenue = allOrders.filter { it.status == "Delivered" }.sumOf { it.totalPrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "HQ Control Console 🛡️",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "App-wide metrics monitoring, registered members, and pending shop verification queues.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Metrics Board
        item {
            Text("Real-Time Economic Flow", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("REVENUE FLOW", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text("₹" + String.format("%.2f", totalRevenue), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Card(modifier = Modifier.weight(1.0f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL ORDERS", style = MaterialTheme.typography.bodySmall)
                        Text(allOrders.size.toString(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MERMBER POOL", style = MaterialTheme.typography.bodySmall)
                        Text(allUsersList.size.toString(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RESTAURANTS", style = MaterialTheme.typography.bodySmall)
                        Text(allShops.size.toString(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        // Approval flow actions
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Pending Merchant Verification Approvals (${pendingShops.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        if (pendingShops.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Approvals list clear. No pending shops.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }

        items(pendingShops) { shop ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(shop.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(shop.cuisineType, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Location coordinates: ${shop.address}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.approveShopAndApprove(shop.id) },
                        modifier = Modifier.fillMaxWidth().testTag("approve_shop_${shop.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify & Approve Live Shop Catalog", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Compliance panel (users registry)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Registered Members Index", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(allUsersList) { usr ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(usr.fullName, fontWeight = FontWeight.Bold)
                            Text("Username: ${usr.username} • Phone: ${usr.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(usr.role, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- FLOATING REAL-TIME SYSTEM NOTIFICATIONS DIALOG DRAWER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogNotifDrawer(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onClearAll()
                onDismiss()
            }) {
                Text("Dismiss & Mark All Read", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Live Updates Feed", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(modifier = Modifier.sizeIn(maxHeight = 320.dp, maxWidth = 280.dp)) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications recorded yet.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(notifications) { notif ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
