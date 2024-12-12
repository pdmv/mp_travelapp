package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminBannerBinding;

import java.util.ArrayList;

public class AdminBannerActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    ActivityAdminBannerBinding binding;
    int id;
    String imagePath;
    Tour selectedTour;
    DatabaseReference bannerRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBannerBinding.inflate(getLayoutInflater());
        bannerRef = database.getReference("Banner");

        setContentView(binding.getRoot());

        initBanner();
        initTour();

        binding.bannerBackBtn.setOnClickListener(v -> finish());
        binding.newBannerSelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.uploadBannerBtn.setOnClickListener(v -> createNewBanner());
    }

    private void banners(ArrayList<SliderItem> sliderItems) {
        binding.viewPagerBanner.setAdapter(new SliderAdapter(sliderItems, binding.viewPagerBanner));
        binding.viewPagerBanner.setClipToPadding(false);
        binding.viewPagerBanner.setClipChildren(false);
        binding.viewPagerBanner.setOffscreenPageLimit(3);
        binding.viewPagerBanner.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        binding.viewPagerBanner.setPageTransformer(compositePageTransformer);
    }

    private void initBanner() {
        binding.progressBarBanner.setVisibility(View.VISIBLE);

        bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void initTour() {
        DatabaseReference tourRef = database.getReference("Tour");

        ArrayAdapter<Tour> tourAdapter = new ArrayAdapter<>(AdminBannerActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminBannerActivity.this, tourRef, tourAdapter, Tour.class);

        binding.newTourSpinner.setAdapter(tourAdapter);

        binding.newTourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Tour selectedTour = (Tour) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    binding.newBannerImageView.setImageURI(selectedImageUri);
                    imagePath = selectedImageUri.toString();
                }
            }
        }
    }

    private void createNewBanner() {
        selectedTour = (Tour) binding.newTourSpinner.getSelectedItem();


        if (imagePath == null) {
            Toast.makeText(AdminBannerActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        bannerRef.orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        SliderItem lastBanner = snapshot.getValue(SliderItem.class);
                        assert lastBanner != null;
                        id = lastBanner.getId() + 1;
                    }
                }

                SliderItem banner = new SliderItem(id, imagePath, selectedTour);

                bannerRef.child(String.valueOf(id)).setValue(banner).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminBannerActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminBannerActivity.this, "Thêm thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminBannerActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}