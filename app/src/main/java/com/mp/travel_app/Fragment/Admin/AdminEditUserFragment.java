package com.mp.travel_app.Fragment.Admin;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.FragmentAdminEditUserBinding;

public class AdminEditUserFragment extends DialogFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private boolean isSaving = false;
    private Users user;

    FragmentAdminEditUserBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference usersRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public AdminEditUserFragment() {
        // Required empty public constructor
    }

    public AdminEditUserFragment(Users user) {
        this.user = user;
    }

    public static AdminEditUserFragment newInstance(String param1, String param2) {
        AdminEditUserFragment fragment = new AdminEditUserFragment();
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

        usersRef = database.getReference("Users");
        storageReference = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminEditUserBinding.inflate(inflater, container, false);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.editImageView.setImageURI(uri);
                binding.editImageView.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        binding.editUsernameTxt.setText(user.getUsername());
        binding.editFullnameTxt.setText(user.getFullname());
        binding.editPhoneNumberTxt.setText(user.getPhoneNumber());
        binding.editEmailTxt.setText(user.getEmail());

        Common.getFileFromFirebase(user.getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(binding.getRoot().getContext())
                        .load(downloadUrl).circleCrop()
                        .into(binding.editImageView);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(binding.getRoot().getContext())
                        .load(user.getAvatar()).circleCrop()
                        .into(binding.editImageView);
            }
        });

        binding.editSelectImageBtn.setOnClickListener(v -> Common.openImagePicker(pickMedia));

        binding.editSaveBtn.setOnClickListener(v -> sendProfileData());

        return binding.getRoot();
    }

    private void sendProfileData() {
        if (!Common.checkIsInProcess(isSaving, binding.editSaveBtn)) {
            return;
        }

        binding.editSaveBtn.setEnabled(false);

        user.setUsername(binding.editUsernameTxt.getText().toString());
        user.setFullname(binding.editFullnameTxt.getText().toString());
        user.setPhoneNumber(binding.editPhoneNumberTxt.getText().toString());
        user.setEmail(binding.editEmailTxt.getText().toString());

        if (!Common.checkFields(binding.getRoot().getContext(), user.getUsername(), user.getFullname(), user.getPhoneNumber(), user.getEmail())) {
            resetSaveButtonState();
            return;
        }

        String currentPassword = user.getPassword();
        String currentPasswordInput = binding.editCurrentPasswordTxt.getText().toString();
        String newPasswordInput = binding.editNewPasswordTxt.getText().toString();
        String reNewPasswordInput = binding.editRePasswordTxt.getText().toString();

        if (currentPasswordInput.isEmpty() || newPasswordInput.isEmpty() || reNewPasswordInput.isEmpty()) {
            Log.d("NoPasswordChange", "User did not change password");
        } else {
            if (Common.checkPassword(currentPasswordInput, currentPassword) && newPasswordInput.equals(reNewPasswordInput)) {
                user.setPassword(Common.hashPassword(binding.editNewPasswordTxt.getText().toString()));
            } else {
                Common.showToast(binding.getRoot().getContext(), "Passwords do not match, please try again", Toast.LENGTH_SHORT);
                resetSaveButtonState();
                return;
            }
        }

        Object tag = binding.editImageView.getTag();

        if (tag == null) {
            updateUser(user);
        } else {
            Common.handleImageUpload(Uri.parse(tag.toString()), new Common.OnImageUploadListener() {
                @Override
                public void onUploadSuccess(String imagePath) {
                    user.setAvatar(imagePath);
                    updateUser(user);
                }

                @Override
                public void onUploadFailed(String errorMessage) {
                    Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                    resetSaveButtonState();
                    dismiss();
                }
            });
        }
    }

    private void updateUser(Users user) {
        String id = user.getId();

        if (id != null) {
            usersRef.child(id).setValue(user).addOnSuccessListener(aVoid -> {
                Common.showToast(binding.getRoot().getContext(), "Save success", Toast.LENGTH_SHORT);
                dismiss();
            }).addOnFailureListener(e -> {
                Common.showToast(binding.getRoot().getContext(), "Save failure", Toast.LENGTH_SHORT);
                dismiss();
            });
        }
    }

    private void resetSaveButtonState() {
        isSaving = false;
        binding.editSaveBtn.setEnabled(true);
    }
}