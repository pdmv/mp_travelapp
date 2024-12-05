package com.example.travel_app.Activity.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Adapter.CategoryAdapter;
import com.example.travel_app.Domain.Category;
import com.example.travel_app.databinding.ActivityAdminCategoryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    ActivityAdminCategoryBinding binding;
    int id;
    String imagePath, name;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference categoryRef = database.getReference("Category");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCategoryBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        initCategory();

        binding.categoryBackBtn.setOnClickListener(v -> finish());
        binding.newCategorySelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.uploadCategoryBtn.setOnClickListener(v -> createNewCategory());
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
                                AdminCategoryActivity.this,
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
                    binding.newCategoryImageView.setImageURI(selectedImageUri);
                    imagePath = selectedImageUri.toString();
                }
            }
        }
    }

    private void createNewCategory() {
        name = binding.newCategoryNameTxt.getText().toString();

        if (name.isEmpty() || imagePath == null) {
            Toast.makeText(AdminCategoryActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryRef.orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Category lastCategory = snapshot.getValue(Category.class);
                        id = lastCategory.getId() + 1;
                    }
                }

                Category category = new Category(id, imagePath, name);

                categoryRef.child(String.valueOf(id)).setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminCategoryActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminCategoryActivity.this, "Thêm thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminCategoryActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}