package com.mp.travel_app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Adapter.PopularAdapter;
import com.mp.travel_app.Adapter.RecommendedAdapter;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.ItemDomain;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.R;
import com.mp.travel_app.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initLocation();
        initBanner();
        initCategory();
        initRecommended();
        initPopular();
    }

    private void initPopular() {
        DatabaseReference databaseReference = database.getReference("Popular");
        binding.progressBarPopular.setVisibility(View.VISIBLE);


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<ItemDomain> itemDomains = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemDomain itemDomain = dataSnapshot.getValue(ItemDomain.class);
                        itemDomains.add(itemDomain);
                    }

                    if (!itemDomains.isEmpty()) {
                        binding.recyclerViewPopular.setLayoutManager(new LinearLayoutManager(
                                MainActivity.this,
                                RecyclerView.HORIZONTAL,
                                false
                        ));
                        RecyclerView.Adapter<PopularAdapter.Viewholder> popularAdapter
                                = new PopularAdapter(itemDomains);
                        binding.recyclerViewPopular.setAdapter(popularAdapter);
                    }

                    binding.progressBarPopular.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initRecommended() {
        DatabaseReference databaseReference = database.getReference("Item");
        binding.progressBarRecommended.setVisibility(View.VISIBLE);


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<ItemDomain> itemDomains = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemDomain itemDomain = dataSnapshot.getValue(ItemDomain.class);
                        itemDomains.add(itemDomain);
                    }

                    if (!itemDomains.isEmpty()) {
                        binding.recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(
                                MainActivity.this,
                                RecyclerView.HORIZONTAL,
                                false
                        ));
                        RecyclerView.Adapter<RecommendedAdapter.Viewholder> recommendedAdapter
                                = new RecommendedAdapter(itemDomains);
                        binding.recyclerViewRecommended.setAdapter(recommendedAdapter);
                    }

                    binding.progressBarRecommended.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initCategory() {
        DatabaseReference databaseReference = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Category> categories = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Category category = dataSnapshot.getValue(Category.class);
                        categories.add(category);
                    }

                    if (!categories.isEmpty()) {
                        binding.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(
                                MainActivity.this,
                                RecyclerView.HORIZONTAL,
                                false
                        ));
                        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> categoryAdapter
                                = new CategoryAdapter(categories);
                        binding.recyclerViewCategory.setAdapter(categoryAdapter);
                    }

                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initLocation() {
        DatabaseReference databaseReference = database.getReference("Location");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Location> locations = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Location location = dataSnapshot.getValue(Location.class);
                        locations.add(location);
                    }

                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, locations);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.locationSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initBanner() {
        DatabaseReference bannerRef = database.getReference("Banner");

        binding.progressBarBanner.setVisibility(View.VISIBLE);


        bannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<SliderItem> sliderItems = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SliderItem sliderItem = dataSnapshot.getValue(SliderItem.class);
                        sliderItems.add(sliderItem);
                    }

                    if (!sliderItems.isEmpty()) {
                        binding.recyclerViewBanner.setLayoutManager(new LinearLayoutManager(
                                MainActivity.this,
                                RecyclerView.HORIZONTAL,
                                false
                        ));
                        RecyclerView.Adapter<SliderAdapter.SliderViewHolder> sliderAdapter
                                = new SliderAdapter(sliderItems);
                        binding.recyclerViewBanner.setAdapter(sliderAdapter);
                    }

                    binding.progressBarBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}