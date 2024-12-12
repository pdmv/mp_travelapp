package com.example.travel_app.Activity.Admin;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.travel_app.Activity.BaseActivity;
import com.example.travel_app.Domain.Location;
import com.example.travel_app.Domain.TourGuide;
import com.example.travel_app.Utils.LoadData;
import com.example.travel_app.databinding.ActivityAdminLocationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLocationActivity extends BaseActivity {
    ActivityAdminLocationBinding binding;
    int id;
    String loc;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference locationRef = database.getReference("Location");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLocationBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        initLocation();

        binding.uploadLocBtn.setOnClickListener(v -> createNewLocation());
        binding.locationBackBtn.setOnClickListener(v -> finish());
    }

    private void initLocation() {
        binding.progressBarLocation.setVisibility(View.VISIBLE);

        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(AdminLocationActivity.this, android.R.layout.simple_list_item_1);
        LoadData.loadDataFromDatabase(AdminLocationActivity.this, locationRef, locationAdapter, Location.class);
        binding.locationList.setAdapter(locationAdapter);

        binding.progressBarLocation.setVisibility(View.GONE);
    }

    private void createNewLocation() {
        loc = binding.newLocationTxt.getText().toString();

        if (loc.isEmpty()) {
            Toast.makeText(AdminLocationActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        locationRef.orderByChild("id").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Location lastLocation = snapshot.getValue(Location.class);
                        id = lastLocation.getId() + 1;
                    }
                }

                Location location = new Location(id, loc);

                locationRef.child(String.valueOf(id)).setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminLocationActivity.this, "Đã thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminLocationActivity.this, "Thêm thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminLocationActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}