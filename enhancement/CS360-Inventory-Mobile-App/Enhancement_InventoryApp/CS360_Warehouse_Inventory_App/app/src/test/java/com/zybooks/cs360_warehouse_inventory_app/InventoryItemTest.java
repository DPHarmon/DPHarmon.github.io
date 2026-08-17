package com.zybooks.cs360_warehouse_inventory_app;

import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryItemTest {

    @Test
    public void constructor_setsNameAndQuantity() {
        InventoryItem item = new InventoryItem("Test Item", 7);
        assertEquals("Test Item", item.getName());
        assertEquals(7, item.getQuantity());
    }

    @Test
    public void setQuantity_updateValue() {
        InventoryItem item = new InventoryItem("Test Item", 7);
        item.setQuantity(11);
        assertEquals(11, item.getQuantity());
    }

    @Test
    public void setQuantity_allowsZero() {
        InventoryItem item = new InventoryItem("Text Item", 7);
        item.setQuantity(0);
        assertEquals(0, item.getQuantity());
    }

    @Test
    public  void setName_updatesValues() {
        InventoryItem item = new InventoryItem("Footlong", 2);
        item.setName("Quarter Pounder");
        assertEquals("Quarter Pounder", item.getName());
    }

}
