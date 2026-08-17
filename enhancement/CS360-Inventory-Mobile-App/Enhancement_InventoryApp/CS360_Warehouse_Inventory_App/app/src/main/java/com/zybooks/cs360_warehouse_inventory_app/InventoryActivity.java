package com.zybooks.cs360_warehouse_inventory_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InventoryActivity extends AppCompatActivity {

    RecyclerView            recyclerViewInventory;
    FloatingActionButton    fabAddItem;
    InventoryAdapter        adapter;
    InventoryViewModel      viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        recyclerViewInventory   = findViewById(R.id.recyclerViewInventory);
        fabAddItem              = findViewById(R.id.fabAddItem);

        //Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Set RecyclerView
        adapter = new InventoryAdapter(new java.util.ArrayList<>(), viewModel);
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInventory.setAdapter(adapter);

        // Track LiveData - list updates automatically with db changes
        viewModel.getAllItems().observe(this, items -> {
            adapter.updateList(items);
            checkForZeroQuantity(items);
        });

        // Floating Action Button
        fabAddItem.setOnClickListener(v -> showAddItemDialog());

    }

    /**
     *  Shows the add-item dialog, validates the name and quantity, and inserts the item.
     *  Invalid input shows a message and keeps the dialog open so the user does not lose what
     *  they typed: a non-numeric quantity is rejected instead of crashing.
     */
    private  void showAddItemDialog() {
        View addItemDialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_item, null);

        EditText editNewItemName            = addItemDialogView.findViewById(R.id.editNewItemName);
        EditText editNewItemQuantity        = addItemDialogView.findViewById(R.id.editNewItemQuantity);

        AlertDialog dialog  = new AlertDialog.Builder(this)
                .setTitle("Add Inventory Item")
                .setView(addItemDialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        //Override the Add button after the dialog shows so invalid input does
        // not auto-dismiss it
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name        = editNewItemName.getText().toString().trim();
                    String quantityStr = editNewItemQuantity.getText().toString().trim();

                    // Validate both fields; show the first problem, keep dialog open.
                    String error = Validators.itemNameError(name);
                    if (error == null) error = Validators.quantityError(quantityStr);
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Safe parse now that the quantity is validated.
                    int quantity = Validators.parseQuantity(quantityStr);
                    viewModel.insert(new InventoryItem(name, quantity));
                    dialog.dismiss();
                }));

        dialog.show();
    }


    // SMS ZERO QUANTITY CHECK
    private void checkForZeroQuantity(java.util.List<InventoryItem> items) {
        // Only send with permission - check SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        for (InventoryItem item : items) {
            if (item.getQuantity() == 0) {
                SmsUtils.sendLowStockAlert(this, item.getName());
            }
        }
    }
}