package com.mp.travel_app.Activity.Admin;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.UsersAdapter;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityAdminUsersBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends BaseActivity {
    private boolean isUploading = false;
    public static final String[] roles = {"Admin", "Customer", "TourGuide"};

    ActivityAdminUsersBinding binding;
    DatabaseReference usersRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUsersBinding.inflate(getLayoutInflater());

        usersRef = database.getReference("Users");
        storageReference = storage.getReference();

        initRoleChoice();
        initUsers();

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.newUserImageView.setImageURI(uri);
                binding.newUserImageView.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        setContentView(binding.getRoot());

        binding.usersBackBtn.setOnClickListener(v -> finish());
        binding.uploadUserBtn.setOnClickListener(v -> sendUserData());
        binding.newUserSelectImageBtn.setOnClickListener(v -> openImagePicker());
    }

    private void initRoleChoice() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                AdminUsersActivity.this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.newUserRoleSpinner.setAdapter(adapter);
    }

    private void initUsers() {
        binding.progressBarUsers.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Users> users = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        users.add(user);
                    }

                    if (!users.isEmpty()) {
                        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(
                                AdminUsersActivity.this,
                                RecyclerView.VERTICAL,
                                false
                        ));
                        RecyclerView.Adapter<UsersAdapter.UsersViewHolder> userAdapter
                                = new UsersAdapter(users);
                        binding.recyclerViewUsers.setAdapter(userAdapter);
                    }

                    binding.progressBarUsers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public interface OnImageUploadListener {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailed(String errorMessage);
    }

    private void handleImageUpload(Uri imageUri, final AdminUsersActivity.OnImageUploadListener listener) {
        if (imageUri == null) {
            return;
        }

        StorageReference imageRef = storageReference.child("images/avatar_" + imageUri.getLastPathSegment());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("UploadImage", "Upload image successfully. URL: " + uri.toString());
                    listener.onUploadSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e("UploadImage", "Get download URL failed", e);
                    listener.onUploadFailed(e.getMessage());
                }));
    }

    private void sendUserData() {
        if (isUploading) {
            return;
        }

        isUploading = true;
        binding.uploadUserBtn.setEnabled(false);

        Users user = new Users();

        user.setFullname(binding.newUserFullNameTxt.getText().toString());
        user.setPhoneNumber(binding.newUserPhoneNumberTxt.getText().toString());
        user.setEmail(binding.newUserEmailTxt.getText().toString());
        user.setUsername(binding.newUserUsernameTxt.getText().toString());
        user.setPassword(
                Common.hashPassword(binding.newUserPasswordTxt.getText().toString()));
        user.setRole(binding.newUserRoleSpinner.getSelectedItem().toString());

        if (user.getFullname().isEmpty() || user.getPhoneNumber().isEmpty() || user.getEmail().isEmpty() ||
                user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            Common.showToast(AdminUsersActivity.this, "Please fill in all information", Toast.LENGTH_SHORT);
            resetUploadButtonState();
            return;
        }

        Query query = usersRef.orderByChild("username").equalTo(user.getUsername());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Common.showToast(AdminUsersActivity.this, "Username already exists", Toast.LENGTH_SHORT);
                    resetUploadButtonState();
                } else {
                    Object tag = binding.newUserImageView.getTag();

                    if (tag == null) {
                        // Default avatar
                        user.setAvatar("https://firebasestorage.googleapis.com/v0/b/travel-app-75022.firebasestorage.app/o/profile.png?alt=media&token=7dbb862d-5513-4549-bf8b-503ac7809655");
                        createUser(user);
                        resetUploadButtonState();
                    } else {
                        handleImageUpload(Uri.parse(tag.toString()), new AdminUsersActivity.OnImageUploadListener() {
                            @Override
                            public void onUploadSuccess(String downloadUrl) {
                                user.setAvatar(downloadUrl);
                                createUser(user);
                                resetUploadButtonState();
                            }

                            @Override
                            public void onUploadFailed(String errorMessage) {
                                Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                                resetUploadButtonState();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("RegisterActivity", error.getMessage());
                resetUploadButtonState();
            }
        });
    }

    private void createUser(Users user) {
        String userId = usersRef.push().getKey();
        if (userId != null) {
            usersRef.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Common.showToast(AdminUsersActivity.this, "Register successfully", Toast.LENGTH_SHORT);
                    binding.newUserFullNameTxt.setText("");
                    binding.newUserPhoneNumberTxt.setText("");
                    binding.newUserEmailTxt.setText("");
                    binding.newUserUsernameTxt.setText("");
                    binding.newUserPasswordTxt.setText("");
                    binding.newUserImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    binding.newUserRoleSpinner.setSelection(0);
                } else {
                    Common.showToast(AdminUsersActivity.this, "Registration failed", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadUserBtn.setEnabled(true);
    }
}