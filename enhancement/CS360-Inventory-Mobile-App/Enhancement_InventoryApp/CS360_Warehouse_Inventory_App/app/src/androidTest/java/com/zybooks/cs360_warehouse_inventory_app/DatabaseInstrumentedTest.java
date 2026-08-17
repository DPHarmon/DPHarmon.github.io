package com.zybooks.cs360_warehouse_inventory_app;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstrumentedTest {

    private AppDatabase db;
    private UserDao     userDao;
    private InventoryDao inventoryDao;

    // @Before runs before every test, creates a new in-memory DB
    // The DB is destroyed after every test
    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
        inventoryDao = db.inventoryDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    // User Tests

    @Test
    public void insertUser_andFindByUsername() {
        User user = new User("Bond", PasswordUtils.hash("bond007"));
        userDao.insert(user);

        User found = userDao.findByUsername("Bond");
        assertNotNull(found);
        assertEquals("Bond", found.getUsername());
    }

    @Test
    public void usernameExists_returnsCorrectCount() {
        User user = new User("Bond", PasswordUtils.hash("bond007"));
        userDao.insert(user);

        int countFound = userDao.usernameExists("Bond");
        assertEquals(1, countFound);

        int countMissing = userDao.usernameExists("Money-Penny");
        assertEquals(0, countMissing);
    }

    @Test
    public  void findByUsername_returnsNull_whenNotFound() {
        User user =  userDao.findByUsername("Not_Bond");
        assertNull(user);
    }

    // Inventory Tests
    @Test
    public void insertItem_andVerifyExists() throws InterruptedException {
        inventoryDao.insert(new InventoryItem("Test Item", 7));

        List<InventoryItem> items = LiveDataTestUtil.getOrAwaitValue(inventoryDao.getAllItems());

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("Test Item", items.get(0).getName());
        assertEquals(7, items.get(0).getQuantity());
    }

    @Test
    public void updateItem_persistsCorrectly() throws InterruptedException {
        inventoryDao.insert(new InventoryItem("Test Item", 7));

        List<InventoryItem> items = LiveDataTestUtil.getOrAwaitValue(
                inventoryDao.getAllItems());
        items.get(0).setQuantity(11);
        inventoryDao.update(items.get(0));

        List<InventoryItem> updated = LiveDataTestUtil.getOrAwaitValue(
                inventoryDao.getAllItems());

        assertEquals(11, updated.get(0).getQuantity());
    }

    @Test
    public void deleteItem_removesFromDatabase() throws InterruptedException {
        inventoryDao.insert(new InventoryItem("Test Item", 7));

        List<InventoryItem> items = LiveDataTestUtil.getOrAwaitValue(
                inventoryDao.getAllItems());

        inventoryDao.delete(items.get(0));

        List<InventoryItem> remaining = LiveDataTestUtil.getOrAwaitValue(
                inventoryDao.getAllItems());

        assertEquals(0, remaining.size());
    }

    @Test
    public void insertMultipleItems_allAppearInList() throws InterruptedException {
        inventoryDao.insert(new InventoryItem("item1", 1));
        inventoryDao.insert(new InventoryItem("item2", 2));
        inventoryDao.insert(new InventoryItem("item3", 3));

        List<InventoryItem> items = LiveDataTestUtil.getOrAwaitValue(
                inventoryDao.getAllItems());
        assertEquals(3, items.size());
        }
}
