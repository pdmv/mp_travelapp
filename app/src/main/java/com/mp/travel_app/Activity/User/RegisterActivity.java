package com.mp.travel_app.Activity.User;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityRegisterBinding;

public class RegisterActivity extends BaseActivity {
    DatabaseReference databaseReference;
    StorageReference storageReference;
    ActivityRegisterBinding binding;
    public static final String[] roles = {"Customer", "TourGuide", "Admin"};
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initRoleChoice();

        databaseReference = database.getReference("Users");
        storageReference = storage.getReference();

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.userAvatar.setImageURI(uri);
                binding.userAvatar.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnToLogin.setOnClickListener(v -> {
            Common.toActivity(RegisterActivity.this, LoginActivity.class);
            finish();
        });
        binding.btnChooseAvatar.setOnClickListener(v -> showImagePicker());
        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void register() {
        if (isRegistering) {
            return;
        }

        isRegistering = true;
        binding.btnRegister.setEnabled(false);

        Users users = new Users();

        users.setFullname(binding.txtFullName.getText().toString());
        users.setPhoneNumber(binding.txtPhoneNumber.getText().toString());
        users.setEmail(binding.txtEmail.getText().toString());
        users.setUsername(binding.txtUsername.getText().toString());
        users.setPassword(
                Common.hashPassword(binding.txtPassword.getText().toString()));

        String role = binding.roleSpinner.getSelectedItem().toString();
        users.setRole(role);

        if (
                users.getFullname().isEmpty() ||
                users.getPhoneNumber().isEmpty() ||
                users.getEmail().isEmpty() ||
                users.getUsername().isEmpty() ||
                users.getPassword().isEmpty()
        ) {
            Common.showToast(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT);
            resetRegisterButtonState();
            return;
        }

        Query query = databaseReference.orderByChild("username").equalTo(users.getUsername());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Common.showToast(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT);
                    resetRegisterButtonState();
                } else {
                    Object tag = binding.userAvatar.getTag();
                    if (tag == null) {
                        // Default avatar
                        users.setAvatar("https://firebasestorage.googleapis.com/v0/b/travel-app-75022.firebasestorage.app/o/profile.png?alt=media&token=7dbb862d-5513-4549-bf8b-503ac7809655");
                        createUser(users);
                        resetRegisterButtonState();
                    } else {
                        handleImageUpload(Uri.parse(tag.toString()), new OnImageUploadListener() {
                            @Override
                            public void onUploadSuccess(String downloadUrl) {
                                users.setAvatar(downloadUrl);
                                createUser(users);
                                resetRegisterButtonState();
                            }

                            @Override
                            public void onUploadFailed(String errorMessage) {
                                Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                                resetRegisterButtonState();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("RegisterActivity", error.getMessage());
                resetRegisterButtonState();
            }
        });
    }

    private void handleImageUpload(Uri imageUri, final OnImageUploadListener listener) {
        if (imageUri == null) {
            return;
        }

        StorageReference imageRef = storageReference.child("images/" + imageUri.getLastPathSegment());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("UploadImage", "Upload image successfully. URL: " + uri.toString());
                    listener.onUploadSuccess(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e("UploadImage", "Get download URL failed", e);
                    listener.onUploadFailed(e.getMessage());
                }))
                .addOnFailureListener(e -> listener.onUploadFailed(e.getMessage()));
    }

    public void createUser(Users user) {
        String userId = databaseReference.push().getKey();
        if (userId != null) {
            databaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Common.showToast(RegisterActivity.this, "Register successfully", Toast.LENGTH_SHORT);
                    Common.toActivity(RegisterActivity.this, LoginActivity.class);
                    finish();
                } else {
                    Common.showToast(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    public interface OnImageUploadListener {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailed(String errorMessage);
    }

    private void showImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void resetRegisterButtonState() {
        isRegistering = false;
        binding.btnRegister.setEnabled(true);
    }

    private void initRoleChoice() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);
    }
}