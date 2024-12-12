package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.LoadData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.databinding.ActivityAdminBannerBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminBannerActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    ActivityAdminBannerBinding binding;
    int id;
    String imagePath;
    Tour selectedTour;
    DatabaseReference bannerRef;
    Uri selectedImageUri;

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

        ArrayAdapter<SliderItem> bannerAdapter = new ArrayAdapter<>(AdminBannerActivity.this, android.R.layout.simple_list_item_1);

        LoadData.loadDataFromDatabaseTest(AdminBannerActivity.this, bannerRef, bannerAdapter, SliderItem.class, new LoadData.DataCallback<SliderItem>() {
            @Override
            public void onDataLoaded(List<SliderItem> sliderItems) {
                banners(new ArrayList<>(sliderItems));
                binding.progressBarBanner.setVisibility(View.GONE);
            }
        });
    }

    private void initTour() {
        DatabaseReference tourRef = database.getReference("Tour");

        ArrayAdapter<Tour> tourAdapter = new ArrayAdapter<>(AdminBannerActivity.this, android.R.layout.simple_spinner_dropdown_item);

        LoadData.loadDataFromDatabaseTest(AdminBannerActivity.this, tourRef, tourAdapter, Tour.class, new LoadData.DataCallback<Tour>() {
            @Override
            public void onDataLoaded(List<Tour> tourList) {
                tourAdapter.clear();
                tourAdapter.addAll(tourList);
                tourAdapter.notifyDataSetChanged();
            }
        });

        binding.newTourSpinner.setAdapter(tourAdapter);

        binding.newTourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedTour = (Tour) parentView.getItemAtPosition(position);
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
                selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    binding.newBannerImageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, final Runnable onSuccess) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef = storageRef.child("img_banner" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imagePath = uri.toString();
                        Log.d("UploadImage", "Image uploaded successfully " + imagePath);
                        onSuccess.run();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadImage", "Image upload failed: ", e);
                    Toast.makeText(AdminBannerActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewBanner() {
        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(selectedImageUri, () -> {
                if (imagePath != null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    int maxId = sharedPreferences.getInt("max_Banner_id", 1);
                    id = maxId + 1;

                    SliderItem banner = new SliderItem(id, imagePath, selectedTour);

                    bannerRef.child(String.valueOf(id)).setValue(banner).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("max_Banner_id", id);
                                editor.apply();

                                binding.newBannerImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                                binding.newTourSpinner.setSelection(0);

                                Toast.makeText(AdminBannerActivity.this, "Added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminBannerActivity.this, "Add failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(AdminBannerActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AdminBannerActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}