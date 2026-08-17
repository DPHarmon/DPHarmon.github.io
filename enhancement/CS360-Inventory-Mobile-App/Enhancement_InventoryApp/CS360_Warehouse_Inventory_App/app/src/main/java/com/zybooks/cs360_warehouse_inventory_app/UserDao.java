package com.zybooks.cs360_warehouse_inventory_app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/* Enhancement - Implement Update User method
*  Purpose - If legacy user, password must update to current user policy. */
@Dao
public interface UserDao {

    // Insert a new user into the database
    @Insert
    void insert(User user);

    /**
     * Updates an existing user: Used to migrate a stored hash to a stronger one.
     *
     * @param user the user to update.
     */
    @Update
    void update(User user);

    // Find user by username - used during login
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    // Check if username already exists - during account creation
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int usernameExists(String username);

}
