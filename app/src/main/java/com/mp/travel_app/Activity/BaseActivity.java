package com.mp.travel_app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class BaseActivity extends AppCompatActivity {
    public static FirebaseDatabase database;
    public static FirebaseStorage storage;
    public static SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        storage = FirebaseStorage
                .getInstance("gs://travel-app-75022.firebasestorage.app");

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}