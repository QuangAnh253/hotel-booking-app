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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        ImageView ivSplashBg = findViewById(R.id.ivSplashBg);
        if (ivSplashBg != null) {
            Glide.with(this).load(R.drawable.bg_lobby).override(720, 1280).into(ivSplashBg);
        }

        new Handler().postDelayed(() -> {
            if (!isNavigated) {
                proceedToApp();
            }
        }, 6000);

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
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (isNavigated) return;
                    isNavigated = true;
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
    }
}