package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;
import com.nhom4.hotelbooking.utils.EmailUtils;

import java.util.Locale;

public class OtpActivity extends AppCompatActivity {

    private EditText[] otpInputs = new EditText[6];
    private MaterialButton btnVerifyOtp;
    private TextView tvOtpDesc, tvTimer, tvResendOtpTimer;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String email, name, phone, uid, mode;
    
    private CountDownTimer expiryTimer;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        getDataFromIntent();
        setupOtpInputs();
        
        startExpiryTimer(5 * 60 * 1000); 
        startResendTimer(30 * 1000);

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvOtpDesc = findViewById(R.id.tvOtpDesc);
        tvTimer = findViewById(R.id.tvTimer);
        tvResendOtpTimer = findViewById(R.id.tvResendOtpTimer);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        otpInputs[0] = findViewById(R.id.otp1);
        otpInputs[1] = findViewById(R.id.otp2);
        otpInputs[2] = findViewById(R.id.otp3);
        otpInputs[3] = findViewById(R.id.otp4);
        otpInputs[4] = findViewById(R.id.otp5);
        otpInputs[5] = findViewById(R.id.otp6);
    }

    private void getDataFromIntent() {
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        uid = getIntent().getStringExtra("uid");
        mode = getIntent().getStringExtra("mode"); 

        if (email != null) {
            tvOtpDesc.setText("Vui lòng nhập mã xác thực được gửi về Email\n" + maskEmail(email));
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        return email.substring(0, 1) + "*******" + email.substring(atIndex - 2);
    }

    private void setupOtpInputs() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) otpInputs[index + 1].requestFocus();
                    checkAllFilled();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().toString().isEmpty() && index > 0) {
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void checkAllFilled() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : otpInputs) sb.append(et.getText().toString());
        btnVerifyOtp.setEnabled(sb.length() == 6);
        if (sb.length() == 6) {
            btnVerifyOtp.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#e8952f")));
            btnVerifyOtp.setTextColor(android.graphics.Color.WHITE);
        } else {
            btnVerifyOtp.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0")));
            btnVerifyOtp.setTextColor(android.graphics.Color.GRAY);
        }
    }

    private void startExpiryTimer(long millis) {
        if (expiryTimer != null) expiryTimer.cancel();
        expiryTimer = new CountDownTimer(millis, 1000) {
            @Override public void onTick(long l) {
                tvTimer.setText(String.format(Locale.getDefault(), "Mã hết hiệu lực trong %02d:%02d", (l/1000)/60, (l/1000)%60));
            }
            @Override public void onFinish() { tvTimer.setText("Mã đã hết hạn"); }
        }.start();
    }

    private void startResendTimer(long millis) {
        if (resendTimer != null) resendTimer.cancel();
        resendTimer = new CountDownTimer(millis, 1000) {
            @Override public void onTick(long l) { tvResendOtpTimer.setText("Gửi lại sau " + l/1000 + "s"); }
            @Override public void onFinish() {
                tvResendOtpTimer.setText("Gửi lại mã xác thực");
                tvResendOtpTimer.setOnClickListener(v -> resendOtp());
            }
        }.start();
    }

    private void resendOtp() {
        String newOtp = String.valueOf((int)(Math.random() * 900000) + 100000);
        db.collection("otp_codes").document(uid).update("code", newOtp, "expiry", System.currentTimeMillis() + 300000);
        String template = "reset_password".equals(mode) ? "template_resetpassword" : "template_otp_hotel";
        
        EmailUtils.sendOtpEmail(email, newOtp, template);
        
        startResendTimer(30000);
        startExpiryTimer(300000);
        Toast.makeText(this, "Đã gửi lại mã xác thực mới", Toast.LENGTH_SHORT).show();
    }

    private void verifyOtp() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : otpInputs) sb.append(et.getText().toString());
        String input = sb.toString();

        db.collection("otp_codes").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && input.equals(snapshot.getString("code"))) {
                if ("reset_password".equals(mode)) {
                    // LUỒNG QUÊN MẬT KHẨU: Gửi link reset của Google
                    mAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Xác minh thành công! Vui lòng kiểm tra hộp thư (bao gồm cả thư rác) để đặt lại mật khẩu.", Toast.LENGTH_LONG).show();
                        db.collection("otp_codes").document(uid).delete();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
                } else {
                    // LUỒNG ĐĂNG KÝ: Lưu user vào Firestore
                    User user = new User(uid, name, email, phone, Constants.ROLE_USER);
                    db.collection(Constants.COLLECTION_USERS).document(uid).set(user).addOnSuccessListener(u -> {
                        db.collection("otp_codes").document(uid).delete();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
                }
            } else {
                Toast.makeText(this, "Mã xác thực không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}