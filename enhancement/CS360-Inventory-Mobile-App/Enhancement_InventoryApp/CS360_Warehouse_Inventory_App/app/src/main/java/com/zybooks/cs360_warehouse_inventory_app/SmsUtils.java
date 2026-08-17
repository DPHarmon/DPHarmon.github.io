package com.zybooks.cs360_warehouse_inventory_app;

import android.Manifest;
import android.content.Context;
import android.telephony.SmsManager;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.util.Log;

/**
 * Sends low-stock SMS alerts. Callers are expected to hold the SEND_SMS
 * permission. This class rechecks it as defense in depth so it never attempts
 * to send without permission even if caller forgets to guard.
 *
 * Date: 7/26/2026
 * Author: Dylan P Harmon
 */
public class SmsUtils {


    private static final String TAG = "SmsUtils";
    // Phone number to send alerts to
    private static final String ALERT_NUMBER ="5551234567";

    /**
     * Texts a low-stock alert for the given item, but only if SEND_SMS is granted.
     * Does nothing (and logs) when the permission is missing or the send fails, so
     * a messaging problem never crashes the caller.
     *
     * @param context a context used to check the permission and send the SMS*
     * @param itemName the name of the item that reached zero quantity
     */
    public static void sendLowStockAlert(Context context, String itemName) {
        // Defense in depth: never attempt to send without permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "SEND_SMS not granted - skipping alert for: " + itemName);
            return;
        }

        try {
            SmsManager smsManager   = SmsManager.getDefault();
            String message          = "CS360 Inventory Alert:  " + itemName +
                                    " has reached zero. Time to restock.";
            smsManager.sendTextMessage(
                    ALERT_NUMBER,       // destination
                    null,               // service center (null = default)
                    message,            // message body
                    null,               // sent intent
                    null                // delivery intent
            );
            Log.d(TAG, "SMS sent for: " + itemName);
        } catch (Exception e) {
            // Log the error
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
        }
    }
}
