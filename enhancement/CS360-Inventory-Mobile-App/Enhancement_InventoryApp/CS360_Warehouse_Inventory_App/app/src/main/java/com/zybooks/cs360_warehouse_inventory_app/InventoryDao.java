package com.zybooks.cs360_warehouse_inventory_app;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface InventoryDao {

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    LiveData<List<InventoryItem>> getAllItems();

    @Insert
    void insert(InventoryItem item);

    @Update
    void update(InventoryItem item);

    @Delete
    void delete(InventoryItem item);



}
