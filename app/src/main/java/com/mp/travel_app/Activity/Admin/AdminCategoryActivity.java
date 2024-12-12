package com.mp.travel_app.Activity.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Domain.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminCategoryBinding;

import java.util.List;

public class AdminCategoryActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    ActivityAdminCategoryBinding binding;
    int id;
    String imagePath, name;
    DatabaseReference categoryRef;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCategoryBinding.inflate(getLayoutInflater());
        categoryRef = database.getReference("Category");

        setContentView(binding.getRoot());

        initCategory();

        binding.categoryBackBtn.setOnClickListener(v -> finish());
        binding.newCategorySelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.uploadCategoryBtn.setOnClickListener(v -> createNewCategory());
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(AdminCategoryActivity.this, android.R.layout.simple_list_item_1);

        LoadData.loadDataFromDatabaseTest(AdminCategoryActivity.this, categoryRef, categoryAdapter, Category.class, new LoadData.DataCallback<Category>() {
            @Override
            public void onDataLoaded(List<Category> categoryList) {
                if (!categoryList.isEmpty()) {
                    binding.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(
                            AdminCategoryActivity.this,
                            RecyclerView.HORIZONTAL,
                            false
                    ));

                    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> categoryAdapter
                            = new CategoryAdapter(categoryList);
                    binding.recyclerViewCategory.setAdapter(categoryAdapter);
                } else {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                }

                binding.progressBarCategory.setVisibility(View.GONE);
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
                    binding.newCategoryImageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, final Runnable onSuccess) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef = storageRef.child("image_tour_" + System.currentTimeMillis() + ".jpg");

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
                    Toast.makeText(AdminCategoryActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewCategory() {
        name = binding.newCategoryNameTxt.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(AdminCategoryActivity.this, "Please fill in all information", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(selectedImageUri, () -> {
                if (imagePath != null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    int maxId = sharedPreferences.getInt("max_Category_id", 1);
                    id = maxId + 1;

                    Category category = new Category(id, imagePath, name);

                    categoryRef.child(String.valueOf(id)).setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("max_Category_id", id);
                                editor.apply();

                                binding.newCategoryNameTxt.setText("");
                                binding.newCategoryImageView.setImageResource(android.R.drawable.ic_menu_gallery);

                                Toast.makeText(AdminCategoryActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminCategoryActivity.this, "Thêm thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(AdminCategoryActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AdminCategoryActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}