package com.mp.travel_app.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mp.travel_app.Fragment.User.HomeFragment;
import com.mp.travel_app.Fragment.User.ProfileFragment;
import com.mp.travel_app.R;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.menuHome) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.menuFavorite) {
                Common.showToast(MainActivity.this, "Choose Favorite", Toast.LENGTH_SHORT);
            } else if (itemId == R.id.menuCart) {
                Common.showToast(MainActivity.this, "Choose Cart", Toast.LENGTH_SHORT);
            } else {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}