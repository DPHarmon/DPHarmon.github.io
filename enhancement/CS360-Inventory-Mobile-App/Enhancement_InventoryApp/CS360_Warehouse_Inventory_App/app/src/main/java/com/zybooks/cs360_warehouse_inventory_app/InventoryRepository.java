package com.zybooks.cs360_warehouse_inventory_app;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 /* Enhancement - Add new method updateUser in service of login migration
 *                Add Javadoc commenting
 * */
public class InventoryRepository {

    private final InventoryDao inventoryDao;
    private final UserDao   userDao;
    private final LiveData<List<InventoryItem>> allItems;

    //Single background thread for all db operations
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

     /**
      * Opens the database and caches the DAOs and the live item list
      *
      * @param context used to obtain the singleton database
      */
    public InventoryRepository(Context context) {
        AppDatabase db  = AppDatabase.getInstance(context);
        inventoryDao    = db.inventoryDao();
        userDao         = db.userDao();
        allItems        = inventoryDao.getAllItems();
    }

    // INVENTORY OPERATIONS

     /**
      *
      * @return a live-updating list of all inventory itmes
      */
    public LiveData<List<InventoryItem>> getAllItems() {
        return allItems;
    }

     /**
      * Inserts an item on the background thread
      *
      * @param item the item to insert
      */
    public void insert(InventoryItem item) {
        executor.execute(() -> inventoryDao.insert(item));
    }

     /**
      * Updates an item on the background thread
      * @param item the item to update
      */
    public void update(InventoryItem item) {
        executor.execute(() -> inventoryDao.update(item));
    }

     /**
      * Deletes an item on the background thread
      *
      * @param item the item to delete
      */
    public void delete(InventoryItem item) {
        executor.execute(() -> inventoryDao.delete(item));
    }

    // User Operations

     /**
      * Inserts a new user on the background thread
      *
      * @param user the user to insert
      */
    public void insertUser(User user) {
        executor.execute(() -> userDao.insert(user));
    }

     /**
      * Updates a user on the background thread: used to persist a re-hash
      *
      * @param user the user to update
      */
     public void updateUser(User user) {
         executor.execute(() -> userDao.update(user));
     }

     /**
      * Finds a user by usernam and returns it via callback.
      *
      * @param username the username to search for
      * @param callback receives the matching User, or null if none exists
      */
    public void findByUsername(String username, UserCallback callback) {
        executor.execute(() -> {
            User user = userDao.findByUsername(username);
            callback.onResult(user);
        });
    }

     /**
      * Checks whether a username is already taken and returns the result via callback
      *
      * @param username the username to check
      * @param callback receives trie if the username exists, otherwise false
      */
    public void usernameExists(String username, ExistsCallback callback) {
        executor.execute(() -> {
            int count = userDao.usernameExists(username);
            callback.onResult(count > 0);
        });
    }


    // Callbacks
      /** Callback delivering a looked up user (or null) back to the caller. */
    public interface UserCallback {
        void onResult(User user);
    }
    /** Callback delivering a boolean - success user exists - result back to the caller. */
    public interface ExistsCallback {
        void onResult(boolean exists);
    }

}
