package com.zybooks.cs360_warehouse_inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * Enhancement - Apply both validators at both login and account creation. Calls to
 * Validators.java to validate input before creating or logging into account.
 */

public class LoginActivity extends AppCompatActivity {
    //Declare EditText Objects
    EditText editUsername;
    EditText editPassword;
    // Declare Button Objects
    Button buttonLogin;
    Button buttonCreateAccount;

    InventoryViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> handleLogin());
        buttonCreateAccount.setOnClickListener(v -> handleCreateAccount());
    }

    private void handleLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validate input before touching the database (defense in depth)
        String error = Validators.usernameError(username);
        if (error == null) error = Validators.passwordError(password);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.login(username, password, user -> {
            //runOnUiThread gets us back to the main thread for UI updates
            runOnUiThread(() -> {
                if (user != null) {
                    // Login Successful - go to SMS permission
                    Intent intent = new Intent(this, SmsPermissionActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(this,
                            "Invalid Username or Password",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void handleCreateAccount() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Enforce the account rules before creating account
        String error = Validators.usernameError(username);
        if(error == null) error = Validators.passwordError(password);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.createAccount(username, password, success -> {
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Account Created. You can now log in",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Username Already Exists.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
