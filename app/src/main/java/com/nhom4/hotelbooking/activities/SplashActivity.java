package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                checkRole(user.getUid());
            }
        }, 2000);
    }

    void checkRole(String uid) {
        db.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    String role = snapshot.getString("role");
                    if (role != null && role.equals(Constants.ROLE_ADMIN)) {
                        startActivity(new Intent(SplashActivity.this, AdminHomeActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                });
    }
}