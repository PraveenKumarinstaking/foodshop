package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): User?

    @Query("SELECT * FROM users")
    fun getAllAsFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE isApproved = 1")
    fun getApprovedShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops")
    fun getAllShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE ownerId = :ownerId")
    fun getShopsByOwner(ownerId: Int): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE id = :id")
    fun getShopById(id: Int): Flow<Shop?>

    @Query("SELECT * FROM shops WHERE id = :id")
    suspend fun getShopByIdSync(id: Int): Shop?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: Shop): Long

    @Update
    suspend fun updateShop(shop: Shop)

    @Query("UPDATE shops SET isApproved = 1 WHERE id = :shopId")
    suspend fun approveShop(shopId: Int)
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE shopId = :shopId AND isAvailable = 1")
    fun getAvailableItemsByShop(shopId: Int): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE shopId = :shopId")
    fun getAllItemsByShop(shopId: Int): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItem): Long

    @Update
    suspend fun updateMenuItem(item: MenuItem)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Int)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersForCustomer(customerId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getOrdersForShop(shopId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE delivererId = :delivererId ORDER BY createdAt DESC")
    fun getOrdersForDeliverer(delivererId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = 'ReadyForPickup' ORDER BY createdAt DESC")
    fun getAvailableOrdersForPickup(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Int): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdSync(id: Int): Order?

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItem): Long

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Int): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: Int): List<OrderItem>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}

@Database(
    entities = [User::class, Shop::class, MenuItem::class, Order::class, OrderItem::class, Notification::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shopDao(): ShopDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitespeed_database"
                )
                .addCallback(DatabaseSeederCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeederCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed base configuration asynchronously on creation
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Introduce a safe delay to let Room finish the creation process and commit the transaction
                    delay(500)
                    val database = getDatabase(context)
                    
                    // 1. Seed standard accounts
                    val adminId = database.userDao().insertUser(
                        User(username = "admin", passwordString = "admin", role = "Admin", fullName = "Namakkal Admin", phone = "+91 98427 00001", address = "Main Fort Area, Namakkal, Tamil Nadu")
                    )
                    val customerId = database.userDao().insertUser(
                        User(username = "customer", passwordString = "customer", role = "Customer", fullName = "Anand Krishnan", phone = "+91 94432 02021", address = "12/4 Mohanur Road, near Bus Stand, Namakkal, Tamil Nadu")
                    )
                    val merchantId = database.userDao().insertUser(
                        User(username = "merchant", passwordString = "merchant", role = "ShopOwner", fullName = "Chef Selvam", phone = "+91 98940 12345", address = "Salem Main Road, Nallipalayam, Namakkal, Tamil Nadu")
                    )
                    val delivererId = database.userDao().insertUser(
                        User(username = "driver", passwordString = "driver", role = "Deliverer", fullName = "Jack Kathiravan", phone = "+91 97890 54321", address = "Paramathi Road, Near Kovilangkal, Namakkal, Tamil Nadu")
                    )

                    // 2. Seed pre-built shops
                    val shop1Id = database.shopDao().insertShop(
                        Shop(ownerId = merchantId.toInt(), name = "Namakkal Egg & Poultry Palace", cuisineType = "Egg Dishes & Biryani", address = "Mohanur Road, Near Anjaneyar Temple, Namakkal", rating = 4.9, imageUrlIndex = 0, isApproved = true)
                    )
                    val shop2Id = database.shopDao().insertShop(
                        Shop(ownerId = merchantId.toInt(), name = "Sri Krishna Bhavan", cuisineType = "South Indian Vegetarian", address = "Salem Road, near Government Hospital, Namakkal", rating = 4.8, imageUrlIndex = 1, isApproved = true)
                    )
                    // A shop that starts as pending, so the Admin can test the Approval workflow!
                    val shop3Id = database.shopDao().insertShop(
                        Shop(ownerId = merchantId.toInt(), name = "Kongu Thalassery Biriyani", cuisineType = "Kongunadu Non-Veg Speciatles", address = "Tiruchengode Road, near High School, Namakkal", rating = 4.4, imageUrlIndex = 2, isApproved = false)
                    )

                    // 3. Seed menu items for Namakkal Egg & Poultry Palace
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop1Id.toInt(), name = "Signature Namakkal Egg Biryani", description = "Fragrant Seeraga Samba rice cooked with exotic spices and farm-fresh boiled eggs.", price = 140.00, category = "Pizza")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop1Id.toInt(), name = "Namakkal Spicy Egg Kalaki", description = "Soft, melt-in-mouth semi-gravy folded egg omelette with rich chicken salna splash.", price = 45.00, category = "Pizza")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop1Id.toInt(), name = "Salem Road Pepper Chicken Fry", description = "Succulent chicken chunks tossed with hand-milled black pepper and authentic Kongu spices.", price = 180.00, category = "Pizza")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop1Id.toInt(), name = "Egg Podimas Paneer Stuffed", description = "Scrambled spiced eggs with a luscious stuffing of grated paneer and fine green chillies.", price = 85.00, category = "Appetizer")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop1Id.toInt(), name = "Traditional Elaneer Payasam", description = "Sweet chilled dessert cooked with tender coconut pulp, rich milk, and green cardamom.", price = 75.00, category = "Dessert")
                    )

                    // 4. Seed menu items for Sri Krishna Bhavan
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop2Id.toInt(), name = "Ghee Roast Masala Dosa", description = "Crispy golden dosa roasted with pure Ghee, stuffed with legacy potato masala. Served with sambar & 3 chutneys.", price = 90.00, category = "Burger")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop2Id.toInt(), name = "MTR Style Fluffy Idli (Plate of 2)", description = "Soft spongy steamed rice cakes served with spicy Karuvelampatti podi, ghee, and local sambar.", price = 50.00, category = "Burger")
                    )
                    database.menuItemDao().insertMenuItem(
                        MenuItem(shopId = shop2Id.toInt(), name = "Namakkal Degree Filter Coffee", description = "Freshly brewed aromatic chicory mixed milk coffee served in a traditional brass dabara and tumbler.", price = 30.00, category = "Appetizer")
                    )

                    // 5. Seed some welcome notifications
                    database.notificationDao().insertNotification(
                        Notification(userId = customerId.toInt(), title = "Welcome to BiteSpeed Namakkal!", message = "Explore authentic Egg specialties and South Indian menus from Poultry Town's best spots. Local delivery is fast!")
                    )
                    database.notificationDao().insertNotification(
                        Notification(userId = merchantId.toInt(), title = "Namakkal ShopOwner Account Active", message = "Begin managing Egg & Poultry Palace or Krishna Bhavan. Add local menus and fulfill incoming local orders.")
                    )
                    database.notificationDao().insertNotification(
                        Notification(userId = delivererId.toInt(), title = "Namakkal Rider Verified", message = "Toggle online to discover local food orders ready for pickup across Mohanur & Salem roads.")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
