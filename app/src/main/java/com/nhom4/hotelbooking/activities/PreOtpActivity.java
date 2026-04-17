package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import java.util.HashMap;
import java.util.Map;

public class PreOtpActivity extends AppCompatActivity {

    private String email, name, phone, uid, mode;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_otp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        uid = intent.getStringExtra("uid");
        mode = intent.getStringExtra("mode"); // "reset_password" hoặc null (đăng ký)

        TextView tvDesc = findViewById(R.id.tvMaskedEmailDesc);
        if (email != null) {
            tvDesc.setText("Để đảm bảo thông tin được bảo mật, Hotel Booking\nsẽ gửi mã xác thực qua Email\n" + maskEmail(email) + " của bạn.");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSendOtp).setOnClickListener(v -> sendOtpAndNavigate());
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        return email.substring(0, 1) + "*******" + email.substring(atIndex - 2);
    }

    private void sendOtpAndNavigate() {
        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        long expiry = System.currentTimeMillis() + 5 * 60 * 1000;

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("code", otpCode);
        otpData.put("expiry", expiry);

        db.collection("otp_codes").document(uid).set(otpData)
                .addOnSuccessListener(unused -> {
                    // Sử dụng template tương ứng
                    String templateId = (mode != null && mode.equals("reset_password")) 
                                        ? "template_resetpassword" : "template_otp_hotel";
                    
                    sendCustomOtpEmail(email, otpCode, templateId);
                    
                    Toast.makeText(this, "Mã OTP đã được gửi", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PreOtpActivity.this, OtpActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("uid", uid);
                    intent.putExtra("mode", mode);
                    intent.putExtra("name", name);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                    finish();
                });
    }

    private void sendCustomOtpEmail(String toEmail, String otpCode, String templateId) {
        new Thread(() -> {
            try {
                String serviceId  = "service_hotel_booking";
                String publicKey  = "qGw9f1tPmRI72Ns1K";
                String jsonBody = "{\"service_id\":\"" + serviceId + "\",\"template_id\":\"" + templateId + "\",\"user_id\":\"" + publicKey + "\",\"template_params\":{\"to_email\":\"" + toEmail + "\",\"otp_code\":\"" + otpCode + "\"}}";
                java.net.URL url = new java.net.URL("https://api.emailjs.com/api/v1.0/email/send");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.getOutputStream().write(jsonBody.getBytes("UTF-8"));
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }
}