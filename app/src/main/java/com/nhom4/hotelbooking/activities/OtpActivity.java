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

public class OtpActivity extends AppCompatActivity {

    EditText edtOtp;
    Button btnVerifyOtp;
    TextView tvOtpDesc, tvResendOtp;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String email, name, phone, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        edtOtp = findViewById(R.id.edtOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpDesc = findViewById(R.id.tvOtpDesc);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Nhận thông tin từ RegisterActivity
        email = getIntent().getStringExtra("email");
        name  = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        uid   = getIntent().getStringExtra("uid");

        tvOtpDesc.setText("Mã OTP đã gửi tới:\n" + email);

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());

        tvResendOtp.setOnClickListener(v -> {
            // Đọc lại OTP từ Firestore rồi gửi lại
            db.collection("otp_codes").document(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String otpCode = snapshot.getString("code");
                            sendOtpEmail(email, otpCode);
                            Toast.makeText(this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    void verifyOtp() {
        String inputOtp = edtOtp.getText().toString().trim();

        if (inputOtp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("otp_codes").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Mã OTP không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String savedOtp = snapshot.getString("code");
                    long expiry     = snapshot.getLong("expiry");

                    // Kiểm tra hết hạn (5 phút)
                    if (System.currentTimeMillis() > expiry) {
                        Toast.makeText(this, "Mã OTP đã hết hạn. Vui lòng gửi lại.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!inputOtp.equals(savedOtp)) {
                        Toast.makeText(this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // OTP đúng → lưu user vào Firestore
                    // name/phone có thể null nếu đến từ LoginActivity (tài khoản chưa xác thực)
                    String safeName  = (name  != null && !name.isEmpty())  ? name  : "Người dùng";
                    String safePhone = (phone != null && !phone.isEmpty()) ? phone : "";
                    User user = new User(uid, safeName, email, safePhone, Constants.ROLE_USER);
                    db.collection(Constants.COLLECTION_USERS).document(uid).set(user)
                            .addOnSuccessListener(unused -> {
                                // Xoá OTP đã dùng
                                db.collection("otp_codes").document(uid).delete();

                                Toast.makeText(this, "Xác nhận thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(OtpActivity.this, MainActivity.class));
                                finish();
                            });
                });
    }

    // Gọi hàm này từ RegisterActivity sau khi tạo tài khoản
    public static void sendOtpEmail(String toEmail, String otpCode) {
        new Thread(() -> {
            try {
                String serviceId  = "service_hotel_booking";
                String templateId = "template_otp_hotel";
                String publicKey  = "qGw9f1tPmRI72Ns1K";

                String jsonBody = "{"
                        + "\"service_id\":\""  + serviceId  + "\","
                        + "\"template_id\":\"" + templateId + "\","
                        + "\"user_id\":\""     + publicKey  + "\","
                        + "\"template_params\":{"
                        + "\"to_email\":\"" + toEmail  + "\","
                        + "\"otp_code\":\""  + otpCode  + "\""
                        + "}}";

                android.util.Log.d("OTP_EMAIL", "Sending to: " + toEmail + " | OTP: " + otpCode);
                android.util.Log.d("OTP_EMAIL", "Body: " + jsonBody);

                java.net.URL url = new java.net.URL("https://api.emailjs.com/api/v1.0/email/send");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                android.util.Log.d("OTP_EMAIL", "Response code: " + responseCode);

                // Đọc response body để xem lỗi cụ thể
                java.io.InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                if (is != null) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    android.util.Log.d("OTP_EMAIL", "Response body: " + sb.toString());
                }

                conn.disconnect();

            } catch (Exception e) {
                android.util.Log.e("OTP_EMAIL", "Exception: " + e.getMessage(), e);
            }
        }).start();
    }
}