package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.TourAdapter;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminTourBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminTourActivity extends BaseActivity {
    ActivityAdminTourBinding binding;

    DatabaseReference tourRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminTourBinding.inflate(getLayoutInflater());
        tourRef = database.getReference("Tour");

        setContentView(binding.getRoot());

        initTour();

        binding.tourCreateBtn.setOnClickListener(v -> startActivity(new Intent(AdminTourActivity.this, AdminCreateTourActivity.class)));
        binding.tourBackBtn.setOnClickListener(v -> finish());
    }

    private void initTour() {
        List<Tour> tours = new ArrayList<>();

        TourAdapter tourAdapter = new TourAdapter(tours);

        LoadData.loadDataIntoRecyclerView(tourRef, binding.recyclerViewTour, binding.progressBarTour,
                binding.noDataTxt, Tour.class, tourAdapter, tours);
    }
}