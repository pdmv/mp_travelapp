package com.mp.travel_app.Activity.Admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Domain.TourGuide;
import com.mp.travel_app.Utils.LoadData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.databinding.ActivityAdminCreateTourBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminCreateTourActivity extends BaseActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ActivityAdminCreateTourBinding binding;
    int id;
    Double price;
    String title, description, duration, dateTour, timeTour, imagePath, priceString;
    TourGuide selectedTourGuide;
    Location selectedLocation;
    Category selectedCategory;
    DatabaseReference tourRef;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCreateTourBinding.inflate(getLayoutInflater());
        tourRef = database.getReference("Tour");

        setContentView(binding.getRoot());

        initLocation();
        initCategory();
        initTourGuide();

        binding.newTourDateTxt.setOnClickListener(v -> showDatePicker());
        binding.newTourTimeTxt.setOnClickListener(v -> showTimePicker());
        binding.newTourSelectImageBtn.setOnClickListener(v -> openImagePicker());
        binding.newTourUploadBtn.setOnClickListener(v -> createNewTour());
        binding.newTourBackBtn.setOnClickListener(v -> finish());
    }

    private void initTourGuide() {
        DatabaseReference tourGuideRef = database.getReference("TourGuide");

        LoadData.loadDataFromDatabaseTest(AdminCreateTourActivity.this, tourGuideRef, new ArrayAdapter<TourGuide>(AdminCreateTourActivity.this, android.R.layout.simple_list_item_1), TourGuide.class, new LoadData.DataCallback<TourGuide>() {
            @Override
            public void onDataLoaded(List<TourGuide> tourGuideList) {
                if (tourGuideList != null && !tourGuideList.isEmpty()) {
                    ArrayAdapter<TourGuide> tourGuideAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item, tourGuideList);
                    binding.newTourGuideSpinner.setAdapter(tourGuideAdapter);
                }
            }
        });
    }

    private void initLocation() {
        DatabaseReference locationRef = database.getReference("Location");

        LoadData.loadDataFromDatabaseTest(AdminCreateTourActivity.this, locationRef, new ArrayAdapter<Location>(AdminCreateTourActivity.this, android.R.layout.simple_list_item_1), Location.class, new LoadData.DataCallback<Location>() {
            @Override
            public void onDataLoaded(List<Location> locationList) {
                if (locationList != null && !locationList.isEmpty()) {
                    ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item, locationList);
                    binding.newTourLocationSpinner.setAdapter(locationAdapter);
                }
            }
        });
    }

    private void initCategory() {
        DatabaseReference categoryRef = database.getReference("Category");

        LoadData.loadDataFromDatabaseTest(AdminCreateTourActivity.this, categoryRef, new ArrayAdapter<Category>(AdminCreateTourActivity.this, android.R.layout.simple_list_item_1), Category.class, new LoadData.DataCallback<Category>() {
            @Override
            public void onDataLoaded(List<Category> categoryList) {
                if (categoryList != null && !categoryList.isEmpty()) {
                    ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
                    binding.newTourCategorySpinner.setAdapter(categoryAdapter);
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AdminCreateTourActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
                    String formattedDate = simpleDateFormat.format(selectedDate.getTime());

                    binding.newTourDateTxt.setText(formattedDate);
                },
                year, month, day
        );

        calendar.add(Calendar.DATE, 0);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                AdminCreateTourActivity.this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = convertTo12HourFormat(hourOfDay, minuteOfHour);
                    binding.newTourTimeTxt.setText(time);
                },
                hour, minute, false
        );

        timePickerDialog.show();
    }

    private String convertTo12HourFormat(int hourOfDay, int minute) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        return sdf.format(calendar.getTime());
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
                selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    binding.newTourImageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, final Runnable onSuccess) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference imageRef = storageRef.child("img_tour_" + System.currentTimeMillis() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imagePath = uri.toString();
                        Log.d("UploadImage", "Image uploaded successfully " + imagePath);
                        onSuccess.run();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadImage", "Image upload failed: ", e);
                    Toast.makeText(AdminCreateTourActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewTour() {
        title = binding.newTourTitleTxt.getText().toString();
        description = binding.newTourDescriptionTxt.getText().toString();
        duration = binding.newTourDurationTxt.getText().toString();
        priceString = binding.newTourPriceTxt.getText().toString();
        dateTour = binding.newTourDateTxt.getText().toString();
        timeTour = binding.newTourTimeTxt.getText().toString();
        selectedTourGuide = (TourGuide) binding.newTourGuideSpinner.getSelectedItem();
        selectedLocation = (Location) binding.newTourLocationSpinner.getSelectedItem();
        selectedCategory = (Category) binding.newTourCategorySpinner.getSelectedItem();

        if (title.isEmpty() || description.isEmpty() || duration.isEmpty() || priceString.isEmpty() || dateTour.isEmpty() || timeTour.isEmpty()) {
            Toast.makeText(AdminCreateTourActivity.this, "Please fill in all information", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(selectedImageUri, () -> {
                if (imagePath != null) {
                    try {
                        price = Double.parseDouble(priceString);
                    } catch (NumberFormatException e) {
                        Log.e("Create Tour: ", "Error transferring total tour fee: ", e);
                        Toast.makeText(AdminCreateTourActivity.this, "Invalid price", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    int maxId = sharedPreferences.getInt("max_Tour_id", 1);
                    id = maxId + 1;

                    Tour tour = new Tour(id, price, title, description, duration, imagePath, dateTour, timeTour, selectedTourGuide, selectedLocation, selectedCategory);

                    tourRef.child(String.valueOf(id)).setValue(tour).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("max_Tour_id", id);
                            editor.apply();

                            binding.newTourTitleTxt.setText("");
                            binding.newTourDescriptionTxt.setText("");
                            binding.newTourDurationTxt.setText("");
                            binding.newTourPriceTxt.setText("");
                            binding.newTourDateTxt.setText("");
                            binding.newTourTimeTxt.setText("");
                            binding.newTourGuideSpinner.setSelection(0);
                            binding.newTourLocationSpinner.setSelection(0);
                            binding.newTourCategorySpinner.setSelection(0);
                            binding.newTourImageView.setImageResource(android.R.drawable.ic_menu_gallery);

                            Toast.makeText(AdminCreateTourActivity.this, "Added successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminCreateTourActivity.this, "Add failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AdminCreateTourActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AdminCreateTourActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}