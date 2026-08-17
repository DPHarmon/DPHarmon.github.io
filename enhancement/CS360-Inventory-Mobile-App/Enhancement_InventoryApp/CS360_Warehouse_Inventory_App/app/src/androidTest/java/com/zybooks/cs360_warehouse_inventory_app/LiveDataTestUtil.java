package com.zybooks.cs360_warehouse_inventory_app;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

///*************************************************************************************
/// Title: unit-testing-room-and-livedata
/// Author: Multiple Users
/// Date: 2025
/// Availability: https://stackoverflow.com/questions/44270688/unit-testing-room-and-livedata
/// When LiveData is returned from a Dao  Room makes the Query asynchronously.
/// For unit tests we want this to be synchronous. LiveDataUtil blocks the test thread, and
/// waits for the value to be passed to the observer, then we can assert on it.
public class LiveDataTestUtil {

    public static <T> T getOrAwaitValue(final LiveData<T> liveData)
        throws InterruptedException {
        final Object [] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        // observeForever must run on main thread
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            liveData.observeForever(new Observer<T>() {
                @Override
                public void onChanged(@Nullable T t) {
                    data[0] = t;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            });
        });

        latch.await(2,TimeUnit.SECONDS);
        // return unchecked
        return(T) data[0];
    }

}
