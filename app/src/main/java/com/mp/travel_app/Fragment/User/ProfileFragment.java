package com.mp.travel_app.Fragment.User;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.MainActivity;
import com.mp.travel_app.Activity.User.ChangePassActivity;
import com.mp.travel_app.Activity.User.LoginActivity;
import com.mp.travel_app.Activity.User.TicketManagementActivity;
import com.mp.travel_app.Activity.User.UpdateInforActivity;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        storage = FirebaseStorage
                .getInstance("gs://travel-app-75022.firebasestorage.app");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        binding.backBtn.setOnClickListener(v -> back());

        Common.getCurrentUser(binding.getRoot().getContext(), new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user != null) {
                    bindingUserInformation(user);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Common.showToast(binding.getRoot().getContext(), errorMessage, Toast.LENGTH_LONG);
                Common.toActivity(binding.getRoot().getContext(), LoginActivity.class);
            }
        });

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                changeAvatar(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            Common.logout(binding.getRoot().getContext(), MainActivity.class);
        });

        binding.btnChangeAvatar.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        binding.btnChangePassword.setOnClickListener(v -> Common.toActivity(binding.getRoot().getContext(), ChangePassActivity.class));
        binding.btnUpdateInformation.setOnClickListener(v -> Common.toActivity(binding.getRoot().getContext(), UpdateInforActivity.class));
        binding.btnTicket.setOnClickListener(v -> Common.toActivity(binding.getRoot().getContext(), TicketManagementActivity.class));

        return binding.getRoot();
    }

    private void bindingUserInformation(Users user) {
        binding.txtRole.setText(user.getRole());
        binding.txtUsername.setText(user.getUsername());

        Common.getFileFromFirebase(user.getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(requireActivity())
                        .load(downloadUrl)
                        .circleCrop()
                        .into(binding.avatarView);
            }

            @Override
            public void onUploadFailed(String errorMessage) {

            }
        });

        binding.txtFullName.setText(user.getFullname());
        binding.txtEmail.setText(user.getEmail());
        binding.txtPhoneNumber.setText(user.getPhoneNumber());
    }

    private void back() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            requireActivity().finish();
        } else {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void changeAvatar(Uri newAvatarUri) {
        binding.btnChangeAvatar.setEnabled(false);
        Common.getCurrentUser(binding.getRoot().getContext(), new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user != null) {
                    Common.deleteFileFromFirebase(user.getAvatar(), new Common.OnDeleteFileListener() {
                        @Override
                        public void onDeleteSuccess() {
                            Common.handleImageUpload(newAvatarUri, new Common.OnImageUploadListener() {
                                @Override
                                public void onUploadSuccess(String imagePath) {
                                    user.setAvatar(imagePath);
                                    Common.updateUserInfo(user, new Common.OnUpdateUserInfoListener() {
                                        @Override
                                        public void onUpdateSuccess() {
                                            bindingUserInformation(user);
                                            binding.btnChangeAvatar.setEnabled(true);
                                            Common.showToast(binding.getRoot().getContext(), "Update avatar successfully", Toast.LENGTH_LONG);
                                        }

                                        @Override
                                        public void onUpdateFailed(String errorMessage) {
                                            Common.showToast(binding.getRoot().getContext(), errorMessage, Toast.LENGTH_LONG);
                                        }
                                    });
                                }

                                @Override
                                public void onUploadFailed(String errorMessage) {

                                }
                            });
                        }

                        @Override
                        public void onDeleteFailed(String errorMessage) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }
}