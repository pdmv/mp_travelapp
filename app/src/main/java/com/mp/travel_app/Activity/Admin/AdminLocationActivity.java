package com.mp.travel_app.Activity.Admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Utils.LoadData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.databinding.ActivityAdminLocationBinding;

import java.util.List;

public class AdminLocationActivity extends BaseActivity {
    ActivityAdminLocationBinding binding;
    int id;
    String loc;
    DatabaseReference locationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLocationBinding.inflate(getLayoutInflater());
        locationRef = database.getReference("Location");

        setContentView(binding.getRoot());

        initLocation();

        binding.uploadLocBtn.setOnClickListener(v -> createNewLocation());
        binding.locationBackBtn.setOnClickListener(v -> finish());
    }

    private void initLocation() {
        binding.progressBarLocation.setVisibility(View.VISIBLE);

        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminLocationActivity.this, android.R.layout.simple_list_item_1);

        LoadData.loadDataFromDatabaseTest(AdminLocationActivity.this, locationRef, locationAdapter, Location.class, new LoadData.DataCallback<Location>() {
            @Override
            public void onDataLoaded(List<Location> locationList) {
                locationAdapter.clear();
                locationAdapter.addAll(locationList);
                locationAdapter.notifyDataSetChanged();
                binding.locationList.setAdapter(locationAdapter);

                binding.progressBarLocation.setVisibility(View.GONE);
            }
        });
    }

    private void createNewLocation() {
        loc = binding.newLocationTxt.getText().toString();

        if (loc.isEmpty()) {
            Toast.makeText(AdminLocationActivity.this, "Please fill in all information", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int maxId = sharedPreferences.getInt("max_Location_id", 1);
        id = maxId + 1;

        Location location = new Location(id, loc);

        locationRef.child(String.valueOf(id)).setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("max_Location_id", id);
                    editor.apply();

                    binding.newLocationTxt.setText("");

                    Toast.makeText(AdminLocationActivity.this, "Added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminLocationActivity.this, "Add failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}