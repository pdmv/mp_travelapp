package com.mp.travel_app.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Activity.User.LoginActivity;
import com.mp.travel_app.Domain.Users;

import org.mindrot.jbcrypt.BCrypt;

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

    public boolean isAuthorized(Context context, String username, String password) {
        DatabaseReference databaseReference = database.getReference("Users");

        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null && checkPassword(password, user.getPassword())) {
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });

        return false;
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

    public static void logout(Context context) {
        sharedPreferences.edit()
                .remove(USERNAME_KEY)
                .remove(PASSWORD_KEY)
                .apply();

        toActivity(context, LoginActivity.class);
    }

    public interface GetUserCallback {
        void onSuccess(Users user);
        void onFailure(String errorMessage);
    }
}
