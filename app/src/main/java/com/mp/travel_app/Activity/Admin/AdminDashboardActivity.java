package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;

import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Activity.MainActivity;
import com.mp.travel_app.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends BaseActivity {
    ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adminLocationBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminLocationActivity.class)));
        binding.adminTourBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminTourActivity.class)));
        binding.adminCategoryBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminCategoryActivity.class)));
        binding.adminBookedBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminTicketActivity.class)));
        binding.adminBannerBtn.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminBannerActivity.class)));
    }
}