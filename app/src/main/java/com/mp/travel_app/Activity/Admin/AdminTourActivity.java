package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.TourAdapter;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.databinding.ActivityAdminTourBinding;

import java.util.ArrayList;

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

        tourRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<Tour> tours = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Tour tour = dataSnapshot.getValue(Tour.class);
                        tours.add(tour);
                    }

                    if (!tours.isEmpty()) {
                        binding.recyclerViewTour.setLayoutManager(new LinearLayoutManager(
                                AdminTourActivity.this,
                                RecyclerView.HORIZONTAL,
                                false
                        ));
                        RecyclerView.Adapter<TourAdapter.TourViewHolder> tourAdapter
                                = new TourAdapter(tours);
                        binding.recyclerViewTour.setAdapter(tourAdapter);
                    }

                    binding.progressBarTour.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}