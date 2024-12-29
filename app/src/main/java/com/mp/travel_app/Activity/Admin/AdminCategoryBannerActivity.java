package com.mp.travel_app.Activity.Admin;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayoutMediator;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.AdminViewPagerAdapter;
import com.mp.travel_app.databinding.ActivityAdminCategoryBannerBinding;

public class AdminCategoryBannerActivity extends BaseActivity {
    ActivityAdminCategoryBannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCategoryBannerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        AdminViewPagerAdapter viewPagerAdapter = new AdminViewPagerAdapter(AdminCategoryBannerActivity.this);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Category");
                    break;
                case 1:
                    tab.setText("Banner");
                    break;
            }
        }).attach();
    }

    @Override
    public void onBackPressed() {
        if (binding.viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            binding.viewPager.setCurrentItem(0, true);
        }
    }
}