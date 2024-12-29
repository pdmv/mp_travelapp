package com.mp.travel_app.Activity.User;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.Admin.AdminDashboardActivity;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Activity.MainActivity;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityLoginBinding;
import android.content.Intent;
import java.util.Objects;

public class LoginActivity extends BaseActivity {
    ActivityLoginBinding binding;
    DatabaseReference databaseReference;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> back());

        binding.btnLogin.setOnClickListener(v -> {
            username = binding.txtUsername.getText().toString();
            password = binding.txtPassword.getText().toString();
            binding.btnLogin.setEnabled(false);
            login();
            binding.btnLogin.setEnabled(true);
        });

        binding.btnToRegister.setOnClickListener(v -> Common.toActivity(LoginActivity.this, RegisterActivity.class));

    }

    public void login() {
        databaseReference = database.getReference("Users");

        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String passwordFromDatabase =
                                dataSnapshot.child("password").getValue(String.class);

                        if (Common.checkPassword(password, passwordFromDatabase)) {
                            // Login successful
                            Common.storeUserCredentials(username, password);

                            if (Objects.equals(dataSnapshot.child("role").getValue(String.class), "Admin")) {
                                Common.toActivity(LoginActivity.this, AdminDashboardActivity.class);
                            } else {
                                Common.toActivity(LoginActivity.this, MainActivity.class);
                            }

                            finish();
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
        resetView();
        finish();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void resetView() {
        binding.txtUsername.setText("");
        binding.txtPassword.setText("");
    }
}