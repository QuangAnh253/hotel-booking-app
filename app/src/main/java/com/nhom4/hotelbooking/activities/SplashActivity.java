package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private boolean isNavigated = false; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // CẤU HÌNH FIRESTORE ỔN ĐỊNH: Bật bộ nhớ đệm để cứu cánh khi mạng lag (S7 Edge)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Nạp ảnh nền an toàn bằng Glide để chống crash OOM
        ImageView ivSplashBg = findViewById(R.id.ivSplashBg);
        if (ivSplashBg != null) {
            Glide.with(this).load(R.drawable.bg_lobby).override(720, 1280).into(ivSplashBg);
        }

        // 1. CƠ CHẾ SAFETY TIMEOUT: Sau 6 giây phải chuyển màn hình, không được treo
        new Handler().postDelayed(() -> {
            if (!isNavigated) {
                Log.d(TAG, "Mạng chậm: Tự động chuyển hướng an toàn");
                proceedToApp();
            }
        }, 6000);

        // 2. Chờ 2 giây cho logo hiện đẹp rồi mới kiểm tra Login
        new Handler().postDelayed(this::proceedToApp, 2000);
    }

    private void proceedToApp() {
        if (isNavigated) return;
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            isNavigated = true;
            startActivity(new Intent(SplashActivity.this, LobbyActivity.class));
            finish();
        } else {
            // Có User: Thử lấy Role từ Firestore
            checkRoleAndNavigate(user.getUid());
        }
    }

    private void checkRoleAndNavigate(String uid) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (isNavigated) return;
                    isNavigated = true;
                    
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        if (Constants.ROLE_ADMIN.equals(role)) {
                            startActivity(new Intent(SplashActivity.this, AdminHomeActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    } else {
                        // Nếu tài khoản lỗi (không có profile): Vào trang khách
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (isNavigated) return;
                    isNavigated = true;
                    // Lỗi mạng Samsung: Vẫn cho vào trang khách để dùng dữ liệu đệm (Cache)
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
    }
}