package com.mp.travel_app.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Users;

import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Method;

public class Common extends BaseActivity {
    public static final String USERNAME_KEY = "USERNAME";
    public static final String PASSWORD_KEY = "PASSWORD";

    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void toActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    public static void changePassword(String username, String oldPassword, String newPassword, final OnChangePasswordListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isPasswordUpdated = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Users user = snapshot.getValue(Users.class);
                        if (user != null && checkPassword(oldPassword, user.getPassword())) {
                            if (!newPassword.equals(oldPassword)) {
                                user.setPassword(hashPassword(newPassword));
                                databaseReference.child(user.getId()).setValue(user).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        listener.onSuccess();
                                    } else {
                                        listener.onFailed("Failed to update password.");
                                    }
                                });
                                isPasswordUpdated = true;
                            } else {
                                listener.onFailed("New password cannot be the same as the old password.");
                            }
                            break;
                        }
                    }
                    if (!isPasswordUpdated) {
                        listener.onFailed("Incorrect old password.");
                    }
                } else {
                    listener.onFailed("User not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailed("Error: " + error.getMessage());
            }
        });
    }

    public interface OnChangePasswordListener {
        void onSuccess();
        void onFailed(String message);
    }

    public interface OnAuthorizationListener {
        void onSuccess();
        void onFailed(String message);
    }

    public void isAuthorized(String username, String password, final OnAuthorizationListener listener) {
        DatabaseReference databaseReference = database.getReference("Users");

        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Users user = snapshot.getValue(Users.class);
                        if (user != null && checkPassword(password, user.getPassword())) {
                            listener.onSuccess();
                            return;
                        }
                    }
                    listener.onFailed("Invalid password.");
                } else {
                    listener.onFailed("User not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailed("Error: " + error.getMessage());
            }
        });
    }

    public static void storeUserCredentials(String username, String password) {
        sharedPreferences.edit()
                .putString(USERNAME_KEY, username)
                .putString(PASSWORD_KEY, password)
                .apply();
    }

    public static void getCurrentUser(Context context, GetUserCallback callback) {
        String username = sharedPreferences.getString(USERNAME_KEY, "");
        String password = sharedPreferences.getString(PASSWORD_KEY, "");

        if (username.isEmpty() || password.isEmpty()) {
            callback.onFailure("Username or password not found in SharedPreferences");
            return;
        }

        getUser(context, username, password, new GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public static void getUser(Context context, String username, String password, GetUserCallback callback) {
        DatabaseReference databaseReference = database.getReference("Users");
        Query query = databaseReference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null && checkPassword(password, user.getPassword())) {
                        callback.onSuccess(user);
                        return;
                    }
                }
                callback.onFailure("User not found or password incorrect");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Error: " + error.getMessage());
            }
        });
    }

    public static void logout(Context context, Class<?> toActivityClass) {
        sharedPreferences.edit()
                .remove(USERNAME_KEY)
                .remove(PASSWORD_KEY)
                .apply();

        toActivity(context, toActivityClass);
    }


    public static void openImagePicker(ActivityResultLauncher<PickVisualMediaRequest> pickMedia) {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public static void handleImageUpload(Uri imageUri, final OnImageUploadListener listener) {
        if (imageUri == null) {
            return;
        }

        StorageReference imageRef = storage.getReference().child("images/" + imageUri.getLastPathSegment());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("UploadImage", "Upload image successfully. URL: " + imageRef.getPath());
                    listener.onUploadSuccess(imageRef.getPath());
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadImage", "Upload image failed: " + e.getMessage());
                    listener.onUploadFailed(e.getMessage());
                });
    }

    public static boolean checkFields(Context context, String... fields) {
        for (String field : fields) {
            if (field.trim().isEmpty()) {
                showToast(context, "Please fill in all information", Toast.LENGTH_SHORT);
                return false;
            }
        }

        return true;
    }

    public static boolean checkIsInProcess(boolean isProcessing, AppCompatButton uploadBtn) {
        if (isProcessing) {
            return false;
        }

        isProcessing = true;
        uploadBtn.setEnabled(false);

        return isProcessing;
    }

    public static <T> void createData(Context context, DatabaseReference databaseReference, T data) {
        String id = databaseReference.push().getKey();

        try {
            Method setIdMethod = data.getClass().getMethod("setId", String.class);
            setIdMethod.invoke(data, id);

            if (id != null) {
                databaseReference.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast(context, "Create success", Toast.LENGTH_SHORT);
                    } else {
                        showToast(context, "Create failure", Toast.LENGTH_SHORT);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", "Error: ", e);
            showToast(context, "Error while creating data", Toast.LENGTH_SHORT);
        }
    }

    public static void getFileFromFirebase(String filePath, final OnGetFileListener listener) {
        if (filePath == null) {
            return;
        }

        StorageReference imageRef = storage.getReference().child(filePath);

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d("GetFile", "Get file successfully. URL: " + uri.toString());
                    listener.onUploadSuccess(uri.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("GetFile", "Get file failed: " + e.getMessage());
                    listener.onUploadFailed(e.getMessage());
                });
    }

    public static void deleteFileFromFirebase(String filePath, final OnDeleteFileListener listener) {
        if (filePath == null) {
            return;
        }

        StorageReference fileRef = storage.getReference().child(filePath);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeleteFile", "Delete file successfully");
                    listener.onDeleteSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteFile", "Delete file failed: " + e.getMessage());
                    listener.onDeleteFailed(e.getMessage());
                });
    }

    public static void updateUserInfo(Users user, final OnUpdateUserInfoListener listener) {
        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.child(user.getId()).setValue(user)
                .addOnCompleteListener(task -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailed(e.getMessage()));
    }



    public interface GetUserCallback {
        void onSuccess(Users user);
        void onFailure(String errorMessage);
    }

    public interface OnImageUploadListener {
        void onUploadSuccess(String imagePath);
        void onUploadFailed(String errorMessage);
    }

    public interface OnGetFileListener {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailed(String errorMessage);
    }

    public interface OnDeleteFileListener {
        void onDeleteSuccess();
        void onDeleteFailed(String errorMessage);
    }

    public interface OnUpdateUserInfoListener {
        void onUpdateSuccess();
        void onUpdateFailed(String errorMessage);
    }

}
