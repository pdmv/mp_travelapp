package com.mp.travel_app.Activity.Admin;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    public static final String[] roleFilterOptions = {"All", "Admin", "TourGuide", "Customer"};
    private final List<Users> users = new ArrayList<>();

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
        initRoleFilter();
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
        binding.newUserSelectImageBtn.setOnClickListener(v -> Common.openImagePicker(pickMedia));
        binding.spinnerRoleFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = parent.getItemAtPosition(position).toString();
                filterUsersByRole(selectedRole);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterUsersByRole("All");
            }
        });
    }

    private void initRoleChoice() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                AdminUsersActivity.this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.newUserRoleSpinner.setAdapter(adapter);
    }

    private void initRoleFilter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                AdminUsersActivity.this, android.R.layout.simple_spinner_item, roleFilterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoleFilter.setAdapter(adapter);
    }

    private void initUsers() {
        binding.progressBarUsers.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    users.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        users.add(user);
                    }

                    filterUsersByRole("All");

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

                    binding.noDataTxt.setVisibility(View.GONE);
                    binding.progressBarUsers.setVisibility(View.GONE);
                } else {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                    binding.progressBarUsers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserData() {
        if (!Common.checkIsInProcess(isUploading, binding.uploadUserBtn)) {
            return;
        }

        Users user = new Users();

        user.setFullname(binding.newUserFullNameTxt.getText().toString());
        user.setPhoneNumber(binding.newUserPhoneNumberTxt.getText().toString());
        user.setEmail(binding.newUserEmailTxt.getText().toString());
        user.setUsername(binding.newUserUsernameTxt.getText().toString());
        user.setPassword(
                Common.hashPassword(binding.newUserPasswordTxt.getText().toString()));
        user.setRole(binding.newUserRoleSpinner.getSelectedItem().toString());

        if (!Common.checkFields(AdminUsersActivity.this, user.getFullname(), user.getPhoneNumber(),
                user.getEmail(), user.getUsername(), user.getPassword())) {
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
                        Common.createData(AdminUsersActivity.this, usersRef, user);
                        resetUploadButtonState();
                    } else {
                        Common.handleImageUpload(Uri.parse(tag.toString()), new Common.OnImageUploadListener() {
                            @Override
                            public void onUploadSuccess(String downloadUrl) {
                                user.setAvatar(downloadUrl);
                                Common.createData(AdminUsersActivity.this, usersRef, user);
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

        binding.newUserFullNameTxt.setText("");
        binding.newUserPhoneNumberTxt.setText("");
        binding.newUserEmailTxt.setText("");
        binding.newUserUsernameTxt.setText("");
        binding.newUserPasswordTxt.setText("");
        binding.newUserImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.newUserImageView.setTag(null);
        binding.newUserRoleSpinner.setSelection(0);
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadUserBtn.setEnabled(true);
    }

    private void filterUsersByRole(String role) {
        List<Users> filteredUsers = new ArrayList<>();

        if ("All".equals(role)) {
            filteredUsers.addAll(users);
        } else {
            for (Users user : users) {
                if (user.getRole().equals(role)) {
                    filteredUsers.add(user);
                }
            }
        }

        UsersAdapter filteredAdapter = new UsersAdapter(filteredUsers);
        binding.recyclerViewUsers.setAdapter(filteredAdapter);
    }
}