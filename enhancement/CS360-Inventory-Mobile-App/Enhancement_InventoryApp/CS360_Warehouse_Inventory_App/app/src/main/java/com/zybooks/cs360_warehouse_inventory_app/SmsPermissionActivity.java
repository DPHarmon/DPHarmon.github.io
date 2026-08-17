package com.zybooks.cs360_warehouse_inventory_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/* Enhancement - Modernize the permission API */

/**
 *  Asks the user for the optional SEND_SMS permission used to text low-stock
 *  alerts.
 *
 *  Enhancement -- replace requestPermissions()/onRequestPersmissionsResult() pair with
 *  modernized ActivityResultLauncher API, and add handling for the permanently-denied
 *  ("Don't ask again") case by routing the user to the app settings.
 *
 *  Date: 07/26/2026
 *  Author: Dylan P Harmon
 */

public class SmsPermissionActivity extends AppCompatActivity {

    private Button buttonGrantSms;
    private Button buttonDenySms;

    // ActivityResultLauncher: Registered as a field per the ActivityResult API guidance
    private final ActivityResultLauncher<String> requestSmsPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // User allowed SMS alerts.
                            toast("SMS permission granted");
                            goToInventory();
                        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this, Manifest.permission.SEND_SMS)) {
                            // Permanently denied: the system won't show the dialog
                            // again, so send the user to Settings to re-enable it.
                            toast("SMS permission is blocked - opening Settings to enable alerts");
                            openAppSettings();
                        } else {
                            // Denied this time, but can be asked again later.
                            toast("SMS permission denied - alerts disabled");
                            goToInventory();
                        }
                    });
    /** Wires up the grant/deny buttons.
     *
     * @param savedInstanceState standard Android saved-state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        buttonGrantSms  = findViewById(R.id.buttonGrantSMS);
        buttonDenySms   = findViewById(R.id.buttonDenySms);

        buttonGrantSms.setOnClickListener(v -> {
            // If already granted, skip straight through; otherwise request it.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                toast("SMS permission already granted");
                goToInventory();
            } else {
                requestSmsPermission.launch(Manifest.permission.SEND_SMS);
            }
        });

        buttonDenySms.setOnClickListener(v -> {
            // SMS is optional so continue on decline
            toast("SMS alerts disabled");
            goToInventory();
        });
    }

    /**
     *  Opens this app's details page in system settings so the user can grant a permission
     *  they previously blocked
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    /**
     *  Advances to the inventory screen and closes current screen
     */
    private void goToInventory() {
        startActivity(new Intent(this, InventoryActivity.class));
        finish();
    }

    /**
     * Shows a short message
     *
     * @param message the text to display
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}