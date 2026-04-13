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
import com.nhom4.hotelbooking.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvGoRegister;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        db.collection(Constants.COLLECTION_USERS).document(uid).get()
                                .addOnSuccessListener(snapshot -> {

                                    // Nếu chưa có trong collection users = chưa xác nhận OTP
                                    if (!snapshot.exists()) {
                                        // Sinh OTP mới và gửi lại thay vì đá user ra
                                        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
                                        long expiry = System.currentTimeMillis() + 5 * 60 * 1000;

                                        java.util.Map<String, Object> otpData = new java.util.HashMap<>();
                                        otpData.put("code", otpCode);
                                        otpData.put("expiry", expiry);

                                        db.collection("otp_codes").document(uid).set(otpData)
                                                .addOnSuccessListener(unused -> {
                                                    OtpActivity.sendOtpEmail(email, otpCode);
                                                    Toast.makeText(this,
                                                            "Tài khoản chưa xác nhận OTP.\nMã mới đã gửi tới " + email,
                                                            Toast.LENGTH_LONG).show();

                                                    Intent otpIntent = new Intent(LoginActivity.this, OtpActivity.class);
                                                    otpIntent.putExtra("email", email);
                                                    otpIntent.putExtra("uid", uid);
                                                    startActivity(otpIntent);
                                                    finish();
                                                });
                                        return;
                                    }

                                    String role = snapshot.getString("role");
                                    if (role != null && role.equals(Constants.ROLE_ADMIN)) {
                                        startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    }
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    });
        });

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}