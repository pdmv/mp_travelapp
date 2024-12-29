package com.mp.travel_app.Fragment.User;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.MainActivity;
import com.mp.travel_app.Activity.User.LoginActivity;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  

        binding.backBtn.setOnClickListener(v -> back());

        Common.getCurrentUser(getActivity(), new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user != null) {
                    bindingUserInformation(user);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Common.showToast(getActivity(), errorMessage, Toast.LENGTH_LONG);
                Common.toActivity(getActivity(), LoginActivity.class);
                getActivity().finish();
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
            Common.logout(getActivity(), MainActivity.class);
            getActivity().finish();
        });

        binding.btnChangeAvatar.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void setContentView(FrameLayout root) {
    }

    private void bindingUserInformation(Users user) {
        binding.txtRole.setText(user.getRole());
        binding.txtUsername.setText(user.getUsername());

        Common.getFileFromFirebase(user.getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(getActivity())
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
        getActivity().finish();
    }

    private void changeAvatar(Uri newAvatarUri) {
        binding.btnChangeAvatar.setEnabled(false);
        Common.getCurrentUser(getActivity(), new Common.GetUserCallback() {
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
                                            Common.showToast(getActivity(), "Update avatar successfully", Toast.LENGTH_LONG);
                                        }

                                        @Override
                                        public void onUpdateFailed(String errorMessage) {
                                            Common.showToast(getActivity(), errorMessage, Toast.LENGTH_LONG);
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
