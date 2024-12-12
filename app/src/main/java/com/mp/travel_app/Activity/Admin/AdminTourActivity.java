package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.TourAdapter;
import com.mp.travel_app.Domain.Tour;
import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminTourBinding;

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
        binding.progressBarTour.setVisibility(View.VISIBLE);

        ArrayAdapter<Tour> tourAdapter = new ArrayAdapter<>(AdminTourActivity.this, android.R.layout.simple_list_item_1);

        LoadData.loadDataFromDatabaseTest(AdminTourActivity.this, tourRef, tourAdapter, Tour.class, new LoadData.DataCallback<Tour>() {
            @Override
            public void onDataLoaded(List<Tour> tourList) {
                if (!tourList.isEmpty()) {
                    binding.recyclerViewTour.setLayoutManager(new LinearLayoutManager(
                            AdminTourActivity.this,
                            RecyclerView.HORIZONTAL,
                            false
                    ));

                    RecyclerView.Adapter<TourAdapter.TourViewHolder> tourAdapter
                            = new TourAdapter(tourList);
                    binding.recyclerViewTour.setAdapter(tourAdapter);
                } else {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                }

                binding.progressBarTour.setVisibility(View.GONE);
            }
        });
    }
}