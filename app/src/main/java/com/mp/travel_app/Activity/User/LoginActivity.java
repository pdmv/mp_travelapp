package com.mp.travel_app.Activity.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.Admin.AdminDashboardActivity;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseActivity {
    ActivityLoginBinding binding;
    DatabaseReference databaseReference;
    public static final String[] roles = {"Admin", "Customer", "TourGuide"};

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        initRoleChoice();

        binding.btnBack.setOnClickListener(v -> back());

        binding.btnLogin.setOnClickListener(v -> {
            username = binding.txtUsername.getText().toString();
            password = binding.txtPassword.getText().toString();
            String role = binding.roleSpinner.getSelectedItem().toString();

            binding.btnLogin.setEnabled(false);
            login(role);
            binding.btnLogin.setEnabled(true);
        });

        binding.btnToRegister.setOnClickListener(v -> {
            Common.toActivity(LoginActivity.this, RegisterActivity.class);
        });
    }

    public void login(String role) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(role);

        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String passwordFromDatabase =
                                dataSnapshot.child("password").getValue(String.class);

                        if (Common.checkPassword(password, passwordFromDatabase)) {
                            // Login success
                            Common.toActivity(LoginActivity.this, AdminDashboardActivity.class);
                        } else {
                            // Login failed
                            Log.d("LoginActivity",
                                    String.format("Login failed. Password: %s, Password from database: %s",
                                            password, passwordFromDatabase));
                            showToast(LoginActivity.this, "Login failed. Please check your password.");
                        }
                    }
                } else {
                    // Login failed
                    Log.d("LoginActivity",
                            String.format("Login failed. Username: %s, Password: %s",
                                    username, password));
                    showToast(LoginActivity.this, "Login failed. Please check your username.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("LoginActivity", error.getMessage());
                showToast(LoginActivity.this, "An error occurred. Please try again later.");
            }
        });
    }

    public void back() {
        finish();
    }

    private void initRoleChoice() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}