package com.zybooks.cs360_warehouse_inventory_app;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import  androidx.room.RoomDatabase;

@Database(
        entities = {
                User.class,
                InventoryItem.class
        },
        version = 1,
        exportSchema = false // Set to True for Production
)

public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract UserDao userDao();
    public abstract InventoryDao inventoryDao();

    // Singleton Instance
    // Volatile ensures instance is always read from main memory
    private static volatile AppDatabase instance;

    // Get or Create the single database instance
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance =  Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                "cs360_warehouse_database"
                    ).build();
                }
            }
        }
        return instance;
    }
}
