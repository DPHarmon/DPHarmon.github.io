package com.zybooks.cs360_warehouse_inventory_app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "inventory_items")
public class InventoryItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "quantity")
    public int quantity;

    // Constructor
    public InventoryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public String getName() {
        return  name;
    }
    public int getQuantity(){
        return quantity;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}