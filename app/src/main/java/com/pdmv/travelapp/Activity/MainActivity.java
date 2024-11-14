package com.pdmv.travelapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pdmv.travelapp.Adapter.CategoryAdapter;
import com.pdmv.travelapp.Adapter.PopularAdapter;
import com.pdmv.travelapp.Adapter.RecommendedAdapter;
import com.pdmv.travelapp.Adapter.SliderAdapter;
import com.pdmv.travelapp.Domain.Category;
import com.pdmv.travelapp.Domain.ItemDomain;
import com.pdmv.travelapp.Domain.Location;
import com.pdmv.travelapp.Domain.SliderItem;
import com.pdmv.travelapp.R;
import com.pdmv.travelapp.databinding.ActivityMainBinding;

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

                    for (DataSnapshot dataSnapshot : snapshot.getChildren())  {
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

                    for (DataSnapshot dataSnapshot : snapshot.getChildren())  {
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

    private void banners(ArrayList<SliderItem> sliderItems) {
        binding.viewPagerSlider.setAdapter(new SliderAdapter(sliderItems, binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    private void initBanner() {
        DatabaseReference databaseReference = database.getReference("Banner");
        binding.progressBarBanner.setVisibility(View.VISIBLE);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<SliderItem> sliderItems = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SliderItem sliderItem = dataSnapshot.getValue(SliderItem.class);
                        sliderItems.add(sliderItem);
                    }

                    banners(sliderItems);
                    binding.progressBarBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}