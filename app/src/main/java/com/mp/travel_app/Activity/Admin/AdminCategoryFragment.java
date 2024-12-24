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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.FragmentAdminCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private boolean isUploading = false;

    AdminCategoryBannerActivity.OnImageUploadListener onImageUploadListener;
    FragmentAdminCategoryBinding binding;

    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference categoryRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public void setOnImageUploadListener(AdminCategoryBannerActivity.OnImageUploadListener listener) {
        this.onImageUploadListener = listener;
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        binding.categoryBackBtn.setOnClickListener(v -> getActivity().finish());
        binding.newCategorySelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.uploadCategoryBtn.setOnClickListener(v -> sendCategoryData());

        return binding.getRoot();
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);

        categoryRef.addValueEventListener(new ValueEventListener() {
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
                                requireContext(),
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

    private void handleImageUpload(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        StorageReference imageRef = storageReference.child("images/category_" + imageUri.getLastPathSegment());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("UploadImage", "Upload image successfully. URL: " + uri.toString());
                    if (onImageUploadListener != null) {
                        onImageUploadListener.onUploadSuccess(uri.toString());
                        resetUploadButtonState();
                    }

                    createCategory(uri.toString());
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

    private void sendCategoryData() {
        if (isUploading) {
            return;
        }

        isUploading = true;
        binding.uploadCategoryBtn.setEnabled(false);

        Object tag = binding.newCategoryImageView.getTag();

        if (tag == null) {
            Toast.makeText(getContext(), "Please fill in all information", Toast.LENGTH_SHORT).show();
            resetUploadButtonState();
            return;
        }

        handleImageUpload(Uri.parse(tag.toString()));
    }

    private void createCategory(String imageUrl) {
        Category newCategory = new Category();

        newCategory.setName(binding.newCategoryNameTxt.getText().toString());
        newCategory.setImagePath(imageUrl);

        if (newCategory.getName().isEmpty()) {
            Common.showToast(getContext(), "Please fill in all information", Toast.LENGTH_SHORT);
            resetUploadButtonState();
            return;
        }

        String categoryId = categoryRef.push().getKey();
        if (categoryId != null) {
            categoryRef.child(categoryId).setValue(newCategory).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Common.showToast(getContext(), "Create success", Toast.LENGTH_SHORT);
                    binding.newCategoryImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    binding.newCategoryNameTxt.setText("");
                } else {
                    Common.showToast(getContext(), "Create failure", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadCategoryBtn.setEnabled(true);
    }
}