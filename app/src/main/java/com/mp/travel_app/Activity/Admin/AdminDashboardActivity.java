package com.mp.travel_app.Activity.Admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.databinding.ActivityAdminDashboardBinding;

public class AdminDashboardActivity extends BaseActivity {
    ActivityAdminDashboardBinding binding;
    private static final String RECEIVE_CURRENT_USER_FULLNAME = "CURRENT_USER_FULLNAME";
    private static final String RECEIVE_CURRENT_USER_AVATAR = "CURRENT_USER_AVATAR";

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

        String fullname = getIntent().getStringExtra(RECEIVE_CURRENT_USER_FULLNAME);
        String avatar = getIntent().getStringExtra(RECEIVE_CURRENT_USER_AVATAR);

        if (fullname != null) {
            binding.fullnameTxt.setText("Hi " + fullname);
        }

        if (avatar != null) {
            Glide.with(AdminDashboardActivity.this)
                    .load(avatar).circleCrop()
                    .into(binding.adminAvatar);
        }
    }
}