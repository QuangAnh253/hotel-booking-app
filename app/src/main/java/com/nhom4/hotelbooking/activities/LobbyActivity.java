package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.databinding.ActivityLobbyBinding;

public class LobbyActivity extends AppCompatActivity {

    private ActivityLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // GIẢI PHÁP CHỐNG CRASH TẬN GỐC:
        // Sử dụng Glide để nạp ảnh local giúp tối ưu bộ nhớ.
        // .override() giúp ép ảnh về kích thước màn hình HD thay vì nạp ảnh gốc 4K/8K.
        ImageView ivBackground = findViewById(R.id.ivBackgroundLobby);
        if (ivBackground != null) {
            Glide.with(this)
                    .load(R.drawable.bg_lobby)
                    .override(720, 1280) // Giảm độ phân giải xuống mức an toàn
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Không lưu cache để tiết kiệm bộ nhớ
                    .into(ivBackground);
        }

        binding.btnLoginNow.setOnClickListener(v -> {
            startActivity(new Intent(LobbyActivity.this, LoginActivity.class));
        });

        binding.btnJoinNow.setOnClickListener(v -> {
            startActivity(new Intent(LobbyActivity.this, RegisterActivity.class));
        });
    }
}