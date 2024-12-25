package com.mp.travel_app.Activity.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Activity.User.LoginActivity;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends BaseActivity {
    ActivityAdminDashboardBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adminLocationBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminLocationActivity.class)));
        binding.adminTourBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminTourActivity.class)));
        binding.adminCategoryBannerBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminCategoryBannerActivity.class)));
        binding.adminBookedBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminTicketActivity.class)));
        binding.adminUsersBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminUsersActivity.class)));
        binding.adminLogoutBtn.setOnClickListener(v -> Common.logout(AdminDashboardActivity.this));

        Common.getCurrentUser(this, new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user != null) {
                    binding.fullnameTxt.setText("Hi " + user.getFullname());

                    Glide.with(AdminDashboardActivity.this)
                            .load(user.getAvatar()).circleCrop()
                            .into(binding.adminAvatar);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Common.showToast(AdminDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT);
            }
        });
    }
}