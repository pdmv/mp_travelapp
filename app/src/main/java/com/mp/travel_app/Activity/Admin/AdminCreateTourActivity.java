package com.mp.travel_app.Activity.Admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminCreateTourBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdminCreateTourActivity extends BaseActivity {
    private boolean isUploading = false;

    ActivityAdminCreateTourBinding binding;
    DatabaseReference tourRef, tourGuideRef;
    StorageReference storageReference;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    String priceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCreateTourBinding.inflate(getLayoutInflater());
        tourRef = database.getReference("Tour");
        tourGuideRef = database.getReference("Users");
        storageReference = storage.getReference();

        setContentView(binding.getRoot());

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                binding.newTourImageView.setImageURI(uri);
                binding.newTourImageView.setTag(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        initLocation();
        initCategory();
        initTourGuide();

        binding.newTourDateTxt.setOnClickListener(v -> showDatePicker());
        binding.newTourTimeTxt.setOnClickListener(v -> showTimePicker());
        binding.newTourSelectImageBtn.setOnClickListener(v -> Common.openImagePicker(pickMedia));
        binding.uploadTourBtn.setOnClickListener(v -> sendTourData());
        binding.newTourBackBtn.setOnClickListener(v -> finish());
    }

    private void initTourGuide() {
        ArrayAdapter<Users> tourGuideAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);

        tourGuideRef.orderByChild("role").equalTo("TourGuide").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users tourGuide = snapshot.getValue(Users.class);

                    if (tourGuide != null) {
                        tourGuideAdapter.add(tourGuide);
                    }
                }

                tourGuideAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.newTourGuideSpinner.setAdapter(tourGuideAdapter);
    }

    private void initLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference("Location");

        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminCreateTourActivity.this, locationRef, locationAdapter, Location.class);

        binding.newTourLocationSpinner.setAdapter(locationAdapter);
    }

    private void initCategory() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoryRef = database.getReference("Category");

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(AdminCreateTourActivity.this, android.R.layout.simple_spinner_dropdown_item);
        LoadData.loadDataFromDatabase(AdminCreateTourActivity.this, categoryRef, categoryAdapter, Category.class);

        binding.newTourCategorySpinner.setAdapter(categoryAdapter);
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

    private void sendTourData() {
        if (!Common.checkIsInProcess(isUploading, binding.uploadTourBtn)) {
            return;
        }

        priceString = binding.newTourPriceTxt.getText().toString();

        Tour newTour = new Tour();

        newTour.setTitle(binding.newTourTitleTxt.getText().toString());
        newTour.setDescription(binding.newTourDescriptionTxt.getText().toString());
        newTour.setDuration(binding.newTourDurationTxt.getText().toString());
        newTour.setDateTour(binding.newTourDateTxt.getText().toString());
        newTour.setTimeTour(binding.newTourTimeTxt.getText().toString());
        newTour.setCategory((Category) binding.newTourCategorySpinner.getSelectedItem());
        newTour.setTourGuide((Users) binding.newTourGuideSpinner.getSelectedItem());
        newTour.setLocation((Location) binding.newTourLocationSpinner.getSelectedItem());
        newTour.setPrice(Double.parseDouble(priceString));

        Object tag = binding.newTourImageView.getTag();

        if (!Common.checkFields(AdminCreateTourActivity.this, newTour.getTitle(), newTour.getDescription(), newTour.getDuration(), priceString,
                newTour.getDateTour(), newTour.getTimeTour(), tag != null ? tag.toString() : "")) {
                    resetUploadButtonState();
                    return;
        }

        Common.handleImageUpload(Uri.parse(Objects.requireNonNull(tag).toString()), new Common.OnImageUploadListener() {
            @Override
            public void onUploadSuccess(String imagePath) {
                newTour.setImagePath(imagePath);
                Common.createData(AdminCreateTourActivity.this, tourRef, newTour);
                resetUploadButtonState();
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Log.e("UploadImage", "Upload image failed", new Exception(errorMessage));
                resetUploadButtonState();
            }
        });

        binding.newTourTitleTxt.setText("");
        binding.newTourDescriptionTxt.setText("");
        binding.newTourDurationTxt.setText("");
        binding.newTourDateTxt.setText("");
        binding.newTourTimeTxt.setText("");
        binding.newTourCategorySpinner.setSelection(0);
        binding.newTourGuideSpinner.setSelection(0);
        binding.newTourLocationSpinner.setSelection(0);
        binding.newTourImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        binding.newTourImageView.setTag(null);
        binding.newTourPriceTxt.setText("");
    }

    private void resetUploadButtonState() {
        isUploading = false;
        binding.uploadTourBtn.setEnabled(true);
    }
}