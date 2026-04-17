package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.nhom4.hotelbooking.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText edtNewPassword, edtConfirmNewPassword;
    private MaterialButton btnSavePassword;
    private FirebaseAuth mAuth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        
        // Nhận email từ OtpActivity để thực hiện reset
        email = getIntent().getStringExtra("email");

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Đảm bảo nút luôn bấm được và hiển thị đúng màu
        btnSavePassword.setEnabled(true);
        btnSavePassword.setAlpha(1.0f);

        btnSavePassword.setOnClickListener(v -> {
            String pass = edtNewPassword.getText().toString().trim();
            String confirm = edtConfirmNewPassword.getText().toString().trim();

            if (pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (pass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            confirmResetOnFirebase();
        });
    }

    private void confirmResetOnFirebase() {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Email người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi yêu cầu Reset Password chính thức của Firebase
        // Đây là cách duy nhất và an toàn nhất để đổi mật khẩu khi người dùng quên
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Hệ thống đã gửi link đổi mật khẩu tới " + email + ". Vui lòng kiểm tra email của bạn.", Toast.LENGTH_LONG).show();
                    
                    // Sau khi gửi mail thành công, quay về Login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}