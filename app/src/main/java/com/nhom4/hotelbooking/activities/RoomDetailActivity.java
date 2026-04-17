package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.ReviewAdapter;
import com.nhom4.hotelbooking.models.Review;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomDetailActivity extends AppCompatActivity {

    private ImageView imgRoomDetail;
    private TextView tvName, tvType, tvPrice, tvCapacity, tvDescription, tvStatus, tvNoReviews;
    private ImageButton btnBack, btnFavorite, btnShare;
    private RecyclerView recyclerReviews;
    private FirebaseFirestore db;
    private Room room;
    private boolean isFavorite = false;
    private String userId;
    private List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        room = (Room) getIntent().getSerializableExtra(Constants.EXTRA_ROOM);

        if (room == null) { finish(); return; }

        initViews();
        bindData();
        checkIsFavorite();
        loadReviews();

        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareRoomInfo());
        
        findViewById(R.id.btnBookNow).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivity(intent);
        });
    }

    private void initViews() {
        imgRoomDetail = findViewById(R.id.imgRoomDetail);
        tvName = findViewById(R.id.tvDetailName);
        tvType = findViewById(R.id.tvDetailType);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvCapacity = findViewById(R.id.tvDetailCapacity);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        btnBack = findViewById(R.id.btnBackDetail);
        btnFavorite = findViewById(R.id.btnFavoriteDetail);
        btnShare = findViewById(R.id.btnShareDetail);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerReviews.setAdapter(reviewAdapter);
    }

    private void bindData() {
        tvName.setText(room.getName());
        tvType.setText(room.getType());
        tvCapacity.setText("Sức chứa: " + room.getCapacity() + " người");
        tvDescription.setText(room.getDescription());
        tvStatus.setText("Trạng thái: " + (Constants.STATUS_AVAILABLE.equals(room.getStatus()) ? "Còn phòng" : "Hết phòng"));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvPrice.setText(formatter.format(room.getPrice()));

        Glide.with(this).load(room.getImageUrl()).placeholder(R.mipmap.logo_hotel).into(imgRoomDetail);
    }

    private void shareRoomInfo() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String priceStr = formatter.format(room.getPrice());

        String shareBody = "🏨 *Hotel Booking - Khám phá phòng nghỉ tuyệt vời!*\n\n" +
                "📌 *Tên phòng*: " + room.getName() + "\n" +
                "🛌 *Loại*: " + room.getType() + "\n" +
                "💰 *Giá*: " + priceStr + "/đêm\n" +
                "👨‍👩‍👧 *Sức chứa*: " + room.getCapacity() + " người\n" +
                "📝 *Mô tả*: " + room.getDescription() + "\n\n" +
                "📲 Tải ngay App Hotel Booking để đặt phòng ngay hôm nay!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Thông tin phòng nghỉ");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ phòng qua:"));
    }

    private void loadReviews() {
        db.collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("roomId", room.getId())
                .limit(10)
                .get()
                .addOnSuccessListener(snap -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Review rv = doc.toObject(Review.class);
                        rv.setId(doc.getId());
                        reviewList.add(rv);
                    }
                    Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                    reviewAdapter.notifyDataSetChanged();
                    
                    if (reviewList.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        recyclerReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        recyclerReviews.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkIsFavorite() {
        if (userId == null) return;
        db.collection("favorites").whereEqualTo("userId", userId).whereEqualTo("roomId", room.getId()).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        isFavorite = true;
                        btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                    }
                });
    }

    private void toggleFavorite() {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            db.collection("favorites").whereEqualTo("userId", userId).whereEqualTo("roomId", room.getId()).get()
                    .addOnSuccessListener(snap -> {
                        for (var doc : snap) doc.getReference().delete();
                        isFavorite = false;
                        btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(this, "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> fav = new HashMap<>();
            fav.put("userId", userId);
            fav.put("roomId", room.getId());
            db.collection("favorites").add(fav).addOnSuccessListener(doc -> {
                isFavorite = true;
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(this, "Đã lưu yêu thích", Toast.LENGTH_SHORT).show();
            });
        }
    }
}