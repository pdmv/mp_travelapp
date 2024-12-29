package com.mp.travel_app.Activity.User;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mp.travel_app.R;
import com.mp.travel_app.Utils.Common;

public class ChangePassActivity extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etRewritePassword;
    private Button btnChangePassword;
    private ImageView btnBack;
    private TextView textView22;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);


        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRewritePassword = findViewById(R.id.etRewritePassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBackTo);
        textView22 = findViewById(R.id.textView22);

        // Xử lý sự kiện nút "Quay lại"
        btnBack.setOnClickListener(v -> onBackPressed());

        // Xử lý sự kiện nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String rewritePassword = etRewritePassword.getText().toString().trim();

            // Kiểm tra các trường không được bỏ trống
            if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(rewritePassword)) {
                Toast.makeText(ChangePassActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu mới và xác nhận mật khẩu phải khớp nhau
            if (!newPassword.equals(rewritePassword)) {
                Toast.makeText(ChangePassActivity.this, "Mật khẩu mới và mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu mới phải có độ dài tối thiểu
            if (newPassword.length() < 6) {
                Toast.makeText(ChangePassActivity.this, "Mật khẩu mới phải dài ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi phương thức để thay đổi mật khẩu từ lớp Common
            changePassword(oldPassword, newPassword);
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        // Lấy username hiện tại từ SharedPreferences
        String username = Common.sharedPreferences.getString(Common.USERNAME_KEY, null);

        if (username == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi phương thức changePassword trong Common
        Common.changePassword(username, oldPassword, newPassword, new Common.OnChangePasswordListener() {
            @Override
            public void onSuccess() {
                // Thông báo thành công
                Toast.makeText(ChangePassActivity.this, "Mật khẩu đã được thay đổi thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình trước đó
            }

            @Override
            public void onFailed(String message) {
                // Thông báo lỗi
                Toast.makeText(ChangePassActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
