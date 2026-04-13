package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.fragments.BookingHistoryFragment;
import com.nhom4.hotelbooking.fragments.HomeFragment;
import com.nhom4.hotelbooking.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_booking) {
                loadFragment(new BookingHistoryFragment());
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                return false;   // ← return false để icon không bị "selected" (vì ta mở Activity riêng)
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });


    }

    void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}
