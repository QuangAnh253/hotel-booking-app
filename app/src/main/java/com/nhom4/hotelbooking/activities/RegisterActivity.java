package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtPhone, edtEmail, edtPassword;
    Button btnRegister;
    TextView tvGoLogin;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.length() != 10) {
                Toast.makeText(this, "Số điện thoại phải có 10 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        // Sinh mã OTP 6 số ngẫu nhiên
                        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
                        long expiry = System.currentTimeMillis() + 5 * 60 * 1000; // 5 phút

                        // Lưu OTP vào Firestore
                        java.util.Map<String, Object> otpData = new java.util.HashMap<>();
                        otpData.put("code", otpCode);
                        otpData.put("expiry", expiry);

                        db.collection("otp_codes").document(uid).set(otpData)
                                .addOnSuccessListener(unused -> {
                                    // Gửi mail
                                    OtpActivity.sendOtpEmail(email, otpCode);

                                    Toast.makeText(this, "Mã OTP đã gửi tới " + email, Toast.LENGTH_LONG).show();

                                    // Chuyển sang OtpActivity — CHƯA lưu user vào "users" collection
                                    Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("name", name);
                                    intent.putExtra("phone", phone);
                                    intent.putExtra("uid", uid);
                                    startActivity(intent);
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Email đã tồn tại hoặc không hợp lệ", Toast.LENGTH_SHORT).show();
                    });
        });

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}