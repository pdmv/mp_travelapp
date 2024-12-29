package com.mp.travel_app.Activity.User;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mp.travel_app.R;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Domain.Users;

public class UpdateInforActivity extends AppCompatActivity {

    private EditText editFullName, editEmail, editPhoneNumber;
    private Button btnUpdateInformation;
    private ImageView btnBackTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_infor);

        // Khởi tạo các view
        editFullName = findViewById(R.id.txtFullName);
        editEmail = findViewById(R.id.txtEmail);
        editPhoneNumber = findViewById(R.id.txtPhone);
        btnUpdateInformation = findViewById(R.id.btnUpdate);
        btnBackTo = findViewById(R.id.btnBackTo); // Khởi tạo nút quay lại

        // Xử lý sự kiện nút "Cập nhật"
        btnUpdateInformation.setOnClickListener(v -> {
            String fullName = editFullName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phoneNumber = editPhoneNumber.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(UpdateInforActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_LONG).show();
            } else {
                // Cập nhật thông tin người dùng
                updateUserInfo(fullName, email, phoneNumber);
            }
        });

        // Xử lý sự kiện nút "Quay lại"
        btnBackTo.setOnClickListener(v -> finish());
    }

    private void updateUserInfo(String fullName, String email, String phoneNumber) {
        Common.getCurrentUser(this, new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                if (user != null) {
                    user.setFullname(fullName);
                    user.setEmail(email);
                    user.setPhoneNumber(phoneNumber);

                    // Cập nhật thông tin người dùng
                    Common.updateUserInfo(user, new Common.OnUpdateUserInfoListener() {
                        @Override
                        public void onUpdateSuccess() {
                            Toast.makeText(UpdateInforActivity.this, "Thông tin đã được cập nhật", Toast.LENGTH_LONG).show();
                            finish(); // Quay lại màn hình trước
                        }

                        @Override
                        public void onUpdateFailed(String errorMessage) {
                            Toast.makeText(UpdateInforActivity.this, "Cập nhật thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(UpdateInforActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
