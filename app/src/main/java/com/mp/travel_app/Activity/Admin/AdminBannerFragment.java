package com.mp.travel_app.Activity.Admin;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.FragmentAdminBannerBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminBannerFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private boolean isUploading = false;

    AdminCategoryBannerActivity.OnImageUploadListener onImageUploadListener;
    FragmentAdminBannerBinding binding;

    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference bannerRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public void setOnImageUploadListener(AdminCategoryBannerActivity.OnImageUploadListener listener) {
        this.onImageUploadListener = listener;
    }

    public AdminBannerFragment() {
        // Required empty public constructor
    }

    public static AdminBannerFragment newInstance(String param1, String param2) {
        AdminBannerFragment fragment = new AdminBannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        storage = FirebaseStorage
                .getInstance("gs://travel-app-75022.firebasestorage.app");

        bannerRef = database.getReference("Banner");
        storageReference = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminBannerBinding.inflate(inflater, container, false);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.newBannerImageView.setImageURI(uri);
                binding.newBannerImageView.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        initBanner();
        initTour();

        binding.bannerBackBtn.setOnClickListener(v -> getActivity().finish());
        binding.newBannerSelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.uploadBannerBtn.setOnClickListener(v -> sendBannerData());

        return binding.getRoot();
    }

    private void handleImageUpload(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        StorageReference imageRef = storageReference.child("images/banner_" + imageUri.getLastPathSegment());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("UploadImage", "Upload image successfully. URL: " + uri.toString());
                    if (onImageUploadListener != null) {
                        onImageUploadListener.onUploadSuccess(uri.toString());
                        resetUploadButtonState();
                    }

                    createBanner(uri.toString());
                    resetUploadButtonState();
                }).addOnFailureListener(e -> {
                    Log.e("UploadImage", "Get download URL failed", e);
                    if (onImageUploadListener != null) {
                        onImageUploadListener.onUploadFailed(e.getMessage());
                        resetUploadButtonState();
                    }
                }));
    }

    private void openImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void initBanner() {
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
                                requireContext(),
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

    private void initTour() {
        DatabaseReference tourRef = database.getReference("Tour");

        ArrayAdapter<Tour> tourAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(getContext(), tourRef, tourAdapter, Tour.class);

        binding.newTourSpinner.setAdapter(tourAdapter);
    }

    private void sendBannerData() {
        if (isUploading) {
            return;
        }

        isUploading = true;
        binding.uploadBannerBtn.setEnabled(false);

        Object tag = binding.newBannerImageView.getTag();

        if (tag == null) {
            Common.showToast(getContext(), "Please fill in all information", Toast.LENGTH_SHORT);
            resetUploadButtonState();
            return;
        }

        handleImageUpload(Uri.parse(tag.toString()));
    }

    private void createBanner(String imageUrl) {
        SliderItem newSliderItem = new SliderItem();

        newSliderItem.setTour((Tour) binding.newTourSpinner.getSelectedItem());
        newSliderItem.setUrl(imageUrl);

        String bannerId = bannerRef.push().getKey();
        if (bannerId != null) {
            bannerRef.child(bannerId).setValue(newSliderItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Common.showToast(getContext(), "Create successful banners", Toast.LENGTH_SHORT);
                    binding.newBannerImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    binding.newTourSpinner.setSelection(0);
                } else {
                    Common.showToast(getContext(), "Banner creation failed", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadBannerBtn.setEnabled(true);
    }
}