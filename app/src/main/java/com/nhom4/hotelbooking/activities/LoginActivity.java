package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvGoRegister, tvForgotPassword;
    ImageButton btnBackLogin;

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
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnBackLogin = findViewById(R.id.btnBackLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBackLogin.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        navigateToHomeByRole(uid);
                    })
                    .addOnFailureListener(e -> {
                        btnLogin.setEnabled(true);
                        Log.e(TAG, "Auth Error: " + e.getMessage());
                        Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void navigateToHomeByRole(String uid) {
        db.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        if (Constants.ROLE_ADMIN.equals(role)) {
                            startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        finish();
                    } else {
                        // Trường hợp Auth có nhưng Firestore không có (lỗi dữ liệu)
                        Toast.makeText(this, "Hồ sơ người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        btnLogin.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    // SỬA LỖI: Không cho vào Hub nếu lỗi Firestore, để tránh màn hình trắng
                    btnLogin.setEnabled(true);
                    Log.e(TAG, "Firestore Error: " + e.getMessage());
                    Toast.makeText(this, "Không thể kết nối cơ sở dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}