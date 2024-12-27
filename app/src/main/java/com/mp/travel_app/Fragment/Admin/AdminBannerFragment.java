package com.mp.travel_app.Fragment.Admin;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Objects;

public class AdminBannerFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private boolean isUploading = false;

    FragmentAdminBannerBinding binding;

    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference bannerRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

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

        binding.bannerBackBtn.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.newBannerSelectImageBtn.setOnClickListener(v -> Common.openImagePicker(pickMedia));
        binding.uploadBannerBtn.setOnClickListener(v -> sendBannerData());

        return binding.getRoot();
    }

    private void initBanner() {
        List<SliderItem> sliderItems = new ArrayList<>();

        SliderAdapter sliderAdapter = new SliderAdapter(sliderItems);

        LoadData.loadDataIntoRecyclerView(bannerRef, binding.recyclerViewBanner, binding.progressBarBanner,
                binding.noDataTxt, SliderItem.class, sliderAdapter, sliderItems);
    }

    private void initTour() {
        DatabaseReference tourRef = database.getReference("Tour");

        ArrayAdapter<Tour> tourAdapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(binding.getRoot().getContext(), tourRef, tourAdapter, Tour.class);

        binding.newTourSpinner.setAdapter(tourAdapter);
    }

    private void sendBannerData() {
        if (!Common.checkIsInProcess(isUploading, binding.uploadBannerBtn)) {
            return;
        }

        SliderItem newSliderItem = new SliderItem();
        newSliderItem.setTour((Tour) binding.newTourSpinner.getSelectedItem());

        Object tag = binding.newBannerImageView.getTag();

        if (!Common.checkFields(binding.getRoot().getContext(), tag != null ? tag.toString() : "")) {
            resetUploadButtonState();
            return;
        }

        Common.handleImageUpload(Uri.parse(Objects.requireNonNull(tag).toString()), new Common.OnImageUploadListener() {
            @Override
            public void onUploadSuccess(String imagePath) {
                newSliderItem.setUrl(imagePath);
                Common.createData(binding.getRoot().getContext(), bannerRef, newSliderItem);
                resetUploadButtonState();
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                resetUploadButtonState();
            }
        });

        binding.newBannerImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.newBannerImageView.setTag(null);
        binding.newTourSpinner.setSelection(0);
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadBannerBtn.setEnabled(true);
    }
}