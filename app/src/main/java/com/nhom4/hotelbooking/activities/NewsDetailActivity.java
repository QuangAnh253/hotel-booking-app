package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.utils.Constants;

public class NewsDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        News news = (News) getIntent().getSerializableExtra(Constants.EXTRA_NEWS);
        if (news == null) {
            finish();
            return;
        }

        findViewById(R.id.btnBackNewsDetail).setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvNewsDetailTitle);
        TextView tvDate = findViewById(R.id.tvNewsDetailDate);
        TextView tvContent = findViewById(R.id.tvNewsDetailContent);
        ImageView img = findViewById(R.id.imgNewsDetail);

        tvTitle.setText(news.getTitle());
        tvDate.setText(news.getDate());
        tvContent.setText(news.getContent());
        Glide.with(this).load(news.getImageUrl()).into(img);
    }
}