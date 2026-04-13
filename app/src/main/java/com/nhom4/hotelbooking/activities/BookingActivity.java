package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.database.DatabaseHelper;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    TextView tvRoomName, tvRoomPrice, tvTotalPrice;
    DatePicker datePickerCheckIn, datePickerCheckOut;
    Button btnConfirmBooking;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseHelper dbHelper;

    Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        datePickerCheckIn = findViewById(R.id.datePickerCheckIn);
        datePickerCheckOut = findViewById(R.id.datePickerCheckOut);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        room = (Room) getIntent().getSerializableExtra(Constants.EXTRA_ROOM);

        tvRoomName.setText(room.getName());
        tvRoomPrice.setText(String.format("Giá: %,.0f VNĐ/đêm", room.getPrice()));
        tvTotalPrice.setText("Tổng tiền: 0 VNĐ");

        datePickerCheckIn.init(
                datePickerCheckIn.getYear(),
                datePickerCheckIn.getMonth(),
                datePickerCheckIn.getDayOfMonth(),
                (view, year, month, day) -> tinhTien()
        );

        datePickerCheckOut.init(
                datePickerCheckOut.getYear(),
                datePickerCheckOut.getMonth(),
                datePickerCheckOut.getDayOfMonth(),
                (view, year, month, day) -> tinhTien()
        );

        btnConfirmBooking.setOnClickListener(v -> datPhong());
    }

    void tinhTien() {
        Calendar checkIn = Calendar.getInstance();
        checkIn.set(datePickerCheckIn.getYear(),
                datePickerCheckIn.getMonth(),
                datePickerCheckIn.getDayOfMonth());

        Calendar checkOut = Calendar.getInstance();
        checkOut.set(datePickerCheckOut.getYear(),
                datePickerCheckOut.getMonth(),
                datePickerCheckOut.getDayOfMonth());

        long diff = checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
        long soNgay = diff / (1000 * 60 * 60 * 24);

        if (soNgay <= 0) {
            tvTotalPrice.setText("Check-out phải sau Check-in");
            return;
        }

        double tong = soNgay * room.getPrice();
        tvTotalPrice.setText(String.format("Tổng tiền: %,.0f VNĐ (%d đêm)", tong, soNgay));
    }

    void datPhong() {
        Calendar checkIn = Calendar.getInstance();
        checkIn.set(datePickerCheckIn.getYear(),
                datePickerCheckIn.getMonth(),
                datePickerCheckIn.getDayOfMonth());

        Calendar checkOut = Calendar.getInstance();
        checkOut.set(datePickerCheckOut.getYear(),
                datePickerCheckOut.getMonth(),
                datePickerCheckOut.getDayOfMonth());

        long diff = checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
        long soNgay = diff / (1000 * 60 * 60 * 24);

        if (soNgay <= 0) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String checkInStr = sdf.format(checkIn.getTime());
        String checkOutStr = sdf.format(checkOut.getTime());
        double tongTien = soNgay * room.getPrice();
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("userId", uid);
        bookingData.put("roomId", room.getId());
        bookingData.put("roomName", room.getName());
        bookingData.put("checkIn", checkInStr);
        bookingData.put("checkOut", checkOutStr);
        bookingData.put("totalPrice", tongTien);
        bookingData.put("status", Constants.STATUS_PENDING);

        db.collection(Constants.COLLECTION_BOOKINGS).add(bookingData)
                .addOnSuccessListener(docRef -> {
                    // Lưu vào SQLite local
                    Booking booking = new Booking(docRef.getId(), uid, room.getId(),
                            room.getName(), checkInStr, checkOutStr, tongTien, Constants.STATUS_PENDING);
                    dbHelper.insertBooking(booking);

                    // Cập nhật trạng thái phòng thành "booked"
                    db.collection(Constants.COLLECTION_ROOMS).document(room.getId())
                            .update("status", Constants.STATUS_BOOKED);

                    Toast.makeText(this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
