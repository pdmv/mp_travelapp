package com.mp.travel_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminLocationBinding;

public class AdminLocationActivity extends BaseActivity {
    ActivityAdminLocationBinding binding;
    DatabaseReference locationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLocationBinding.inflate(getLayoutInflater());
        locationRef = database.getReference("Location");

        setContentView(binding.getRoot());

        initLocation();

        binding.uploadLocBtn.setOnClickListener(v -> sendLocationData());
        binding.locationBackBtn.setOnClickListener(v -> finish());
    }

    private void initLocation() {
        binding.progressBarLocation.setVisibility(View.VISIBLE);

        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminLocationActivity.this, android.R.layout.simple_list_item_1);
        LoadData.loadDataFromDatabase(AdminLocationActivity.this, locationRef, locationAdapter, Location.class);
        binding.locationList.setAdapter(locationAdapter);

        binding.progressBarLocation.setVisibility(View.GONE);
    }

    private void sendLocationData() {
        Location newLocation = new Location();

        newLocation.setLoc(binding.newLocationTxt.getText().toString());

        if (!Common.checkFields(AdminLocationActivity.this, newLocation.getLoc())) {
            return;
        }

        Common.createData(AdminLocationActivity.this, locationRef, newLocation);

        binding.newLocationTxt.setText("");
    }
}