package com.mp.travel_app.Activity.Admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Domain.TourGuide;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminCreateTourBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tourGuideRef = database.getReference("TourGuide");

        ArrayAdapter<TourGuide> tourGuideAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminCreateTourActivity.this, tourGuideRef, tourGuideAdapter, TourGuide.class);

        setupSpinner(binding.newTourGuideSpinner, tourGuideAdapter);
    }

    private void initLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("Location");

        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminCreateTourActivity.this, locationRef, locationAdapter, Location.class);

        setupSpinner(binding.newTourLocationSpinner, locationAdapter);
    }

    private void initCategory() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoryRef = database.getReference("Category");

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminCreateTourActivity.this, categoryRef, categoryAdapter, Category.class);

        setupSpinner(binding.newTourCategorySpinner, categoryAdapter);
    }

    private <T> void setupSpinner(Spinner spinner, ArrayAdapter<T> adapter) {
        spinner.setAdapter(adapter);

        // Thiết lập OnItemSelectedListener cho Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                T selectedItem = (T) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

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
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    binding.newTourImageView.setImageURI(selectedImageUri);
                    imagePath = selectedImageUri.toString();
                }
            }
        }
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

        if (title.isEmpty() || description.isEmpty() || duration.isEmpty() || priceString.isEmpty() || dateTour.isEmpty() || timeTour.isEmpty() || imagePath == null) {
            Toast.makeText(AdminCreateTourActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        tourRef.orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Tour lastTour = snapshot.getValue(Tour.class);
                        id = lastTour.getId() + 1;
                    }
                }

                try {
                    price = Double.parseDouble(priceString);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(AdminCreateTourActivity.this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                Tour tour = new Tour(id, price, title, description, duration, imagePath, dateTour, timeTour, selectedTourGuide, selectedLocation, selectedCategory);

                tourRef.child(String.valueOf(id)).setValue(tour).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminCreateTourActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminCreateTourActivity.this, "Thêm thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminCreateTourActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}