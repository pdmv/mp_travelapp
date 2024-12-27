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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.FragmentAdminCategoryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminCategoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private boolean isUploading = false;

    FragmentAdminCategoryBinding binding;

    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference categoryRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public AdminCategoryFragment() {
        // Required empty public constructor
    }

    public static AdminCategoryFragment newInstance(String param1, String param2) {
        AdminCategoryFragment fragment = new AdminCategoryFragment();
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

        categoryRef = database.getReference("Category");
        storageReference = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminCategoryBinding.inflate(inflater, container, false);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.newCategoryImageView.setImageURI(uri);
                binding.newCategoryImageView.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        initCategory();

        binding.categoryBackBtn.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.newCategorySelectImageBtn.setOnClickListener(v -> Common.openImagePicker(pickMedia));
        binding.uploadCategoryBtn.setOnClickListener(v -> sendCategoryData());

        return binding.getRoot();
    }

    private void initCategory() {
        List<Category> categories = new ArrayList<>();

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);

        LoadData.loadDataIntoRecyclerView(categoryRef, binding.recyclerViewCategory, binding.progressBarCategory,
                binding.noDataTxt, Category.class, categoryAdapter, categories);
    }

    private void sendCategoryData() {
        if (!Common.checkIsInProcess(isUploading, binding.uploadCategoryBtn)) {
            return;
        }

        Category newCategory = new Category();
        newCategory.setName(binding.newCategoryNameTxt.getText().toString());

        Object tag = binding.newCategoryImageView.getTag();

        if (!Common.checkFields(binding.getRoot().getContext(), newCategory.getName(), tag != null ? tag.toString() : "")) {
            resetUploadButtonState();
            return;
        }

        Common.handleImageUpload(Uri.parse(Objects.requireNonNull(tag).toString()), new Common.OnImageUploadListener() {
            @Override
            public void onUploadSuccess(String imagePath) {
                newCategory.setImagePath(imagePath);
                Common.createData(binding.getRoot().getContext(), categoryRef, newCategory);
                resetUploadButtonState();
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                resetUploadButtonState();
            }
        });

        binding.newCategoryImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.newCategoryImageView.setTag(null);
        binding.newCategoryNameTxt.setText("");
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadCategoryBtn.setEnabled(true);
    }
}