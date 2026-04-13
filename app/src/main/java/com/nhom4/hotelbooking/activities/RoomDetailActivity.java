package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

public class RoomDetailActivity extends AppCompatActivity {

    ImageView imgRoomDetail;
    TextView tvDetailName, tvDetailType, tvDetailPrice;
    TextView tvDetailCapacity, tvDetailDescription, tvDetailStatus;
    Button btnBookNow;

    FirebaseFirestore db;
    Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        imgRoomDetail = findViewById(R.id.imgRoomDetail);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailType = findViewById(R.id.tvDetailType);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailCapacity = findViewById(R.id.tvDetailCapacity);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnBookNow = findViewById(R.id.btnBookNow);

        db = FirebaseFirestore.getInstance();

        room = (Room) getIntent().getSerializableExtra(Constants.EXTRA_ROOM);

        if (room != null) {
            showRoomData();
            checkRoomStatusRealtime();
        }

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(RoomDetailActivity.this, BookingActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivityForResult(intent, 100);
        });
    }

    void showRoomData() {
        tvDetailName.setText(room.getName());
        tvDetailType.setText("Loại: " + room.getType());
        tvDetailPrice.setText(String.format("%,.0f VNĐ/đêm", room.getPrice()));
        tvDetailCapacity.setText("Sức chứa: " + room.getCapacity() + " người");
        tvDetailDescription.setText(room.getDescription());

        Glide.with(this)
                .load(room.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(imgRoomDetail);
    }

    void checkRoomStatusRealtime() {
        db.collection(Constants.COLLECTION_ROOMS).document(room.getId())
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        String status = snapshot.getString("status");
                        if (status != null && status.equals("maintenance")) {
                            tvDetailStatus.setText("Trạng thái: Đang bảo trì");
                            tvDetailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            btnBookNow.setEnabled(false);
                            btnBookNow.setText("Phòng đang bảo trì");
                        } else {
                            tvDetailStatus.setText("Trạng thái: Có thể đặt");
                            tvDetailStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            btnBookNow.setEnabled(true);
                            btnBookNow.setText("Đặt phòng ngay");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
        }
    }
}
