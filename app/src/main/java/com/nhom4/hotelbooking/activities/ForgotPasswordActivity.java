package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.utils.Constants;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmailForgot;
    private MaterialButton btnConfirmForgot;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = FirebaseFirestore.getInstance();
        edtEmailForgot = findViewById(R.id.edtEmailForgot);
        btnConfirmForgot = findViewById(R.id.btnConfirmForgot);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtEmailForgot.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnConfirmForgot.setEnabled(true);
                    btnConfirmForgot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#e8952f")));
                    btnConfirmForgot.setTextColor(android.graphics.Color.WHITE);
                } else {
                    btnConfirmForgot.setEnabled(false);
                    btnConfirmForgot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0")));
                    btnConfirmForgot.setTextColor(android.graphics.Color.GRAY);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnConfirmForgot.setOnClickListener(v -> checkUserAndSendOtp());
    }

    private void checkUserAndSendOtp() {
        String input = edtEmailForgot.getText().toString().trim();

        // Tìm user trong Firestore theo email (hoặc phone nếu bạn lưu phone trong email field)
        db.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("email", input)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Email này chưa được đăng ký tài khoản", Toast.LENGTH_SHORT).show();
                    } else {
                        String uid = queryDocumentSnapshots.getDocuments().get(0).getId();
                        
                        // Chuyển sang PreOtpActivity với mode Reset Password
                        Intent intent = new Intent(ForgotPasswordActivity.this, PreOtpActivity.class);
                        intent.putExtra("email", input);
                        intent.putExtra("uid", uid);
                        intent.putExtra("mode", "reset_password"); // Đánh dấu là quên mật khẩu
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}