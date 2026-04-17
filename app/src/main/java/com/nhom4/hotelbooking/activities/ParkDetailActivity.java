package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.utils.Constants;
import java.text.NumberFormat;
import java.util.Locale;

public class ParkDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_detail);

        Park park = (Park) getIntent().getSerializableExtra(Constants.EXTRA_PARK);
        if (park == null) {
            finish();
            return;
        }

        findViewById(R.id.btnBackParkDetail).setOnClickListener(v -> finish());

        TextView tvName = findViewById(R.id.tvParkDetailName);
        TextView tvPriceInfo = findViewById(R.id.tvParkDetailPriceInfo);
        TextView tvPriceBottom = findViewById(R.id.tvParkDetailPriceBottom);
        TextView tvDescription = findViewById(R.id.tvParkDetailDescription);
        ImageView img = findViewById(R.id.imgParkDetail);

        tvName.setText(park.getName());
        tvDescription.setText(park.getDescription());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceStr = formatter.format(park.getPrice());
        tvPriceInfo.setText("Giá vé từ " + priceStr);
        tvPriceBottom.setText(priceStr);

        Glide.with(this).load(park.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(img);

        findViewById(R.id.btnBookParkNow).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đặt vé đang được tích hợp!", Toast.LENGTH_SHORT).show();
        });
    }
}