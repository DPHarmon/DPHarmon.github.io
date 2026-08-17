package com.zybooks.cs360_warehouse_inventory_app;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

/* Enhancement to InventoryViewModel - Update Login method
*
* Date: 07/26/2026
* Author: Dylan P Harmon
* */
public class InventoryViewModel extends AndroidViewModel {

    private final InventoryRepository repository;
    private final LiveData<List<InventoryItem>> allItems;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository  = new InventoryRepository(application);
        allItems    = repository.getAllItems();
    }

    //  INVENTORY
    public LiveData<List<InventoryItem>> getAllItems() {

        return allItems;
    }

    public void insert(InventoryItem item) {

        repository.insert(item);
    }

    public void update(InventoryItem item) {

        repository.update(item);
    }
    public void delete(InventoryItem item) {
        repository.delete(item);
    }

    // USER
    /**
     *  Authenticates a user and reports the result via callback.
     *  On success with legacy or weak hash, the stored hash is
     *  upgraded to the currently implemented PBKDF2 Policy
     *
     * @param username the entered username
     * @param password the entered plaintext password
     * @param callback receives the matched user, or null if auth fails
     * */
   public void login(String username, String password,
                     InventoryRepository.UserCallback callback) {
       repository.findByUsername(username, user -> {
           if (user != null && PasswordUtils.verify(password, user.getPasswordHash())) {
               // Successful Auth: upgrade hash without disrupting user
               if(PasswordUtils.needsRehash(user.getPasswordHash())) {
                   user.passwordHash = PasswordUtils.hash(password);
                   repository.updateUser(user);
               }
               callback.onResult(user);
           } else {
               callback.onResult(null);
           }
       });
   }

    public void createAccount(String username, String password,
                              InventoryRepository.ExistsCallback callback) {
        repository.usernameExists(username, exists -> {
            if (exists) {
                // Username already taken
                callback.onResult(false);
            } else {
                // Create new username with hashed password
                User newUser = new User(username, PasswordUtils.hash(password));
                repository.insertUser(newUser);
                callback.onResult(true);
            }
        });
    }
}
