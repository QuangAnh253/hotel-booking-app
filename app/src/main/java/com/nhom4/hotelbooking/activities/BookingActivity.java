package com.nhom4.hotelbooking.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private TextView tvRoomName, tvRoomPrice, tvCheckInDate, tvCheckOutDate, tvAvailabilityStatus, tvTotalPrice;
    private ImageView ivStatusIcon;
    private CardView cardAvailabilityStatus;
    private MaterialButton btnConfirm;
    
    private Room room;
    private Calendar checkInCalendar, checkOutCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private FirebaseFirestore db;
    private boolean isAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();
        room = (Room) getIntent().getSerializableExtra(Constants.EXTRA_ROOM);

        if (room == null) { finish(); return; }

        initViews();
        bindRoomData();

        findViewById(R.id.btnBackBooking).setOnClickListener(v -> finish());
        findViewById(R.id.btnSelectCheckIn).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.btnSelectCheckOut).setOnClickListener(v -> showDatePicker(false));
        
        btnConfirm.setOnClickListener(v -> {
            if (isAvailable) performBooking();
            else Toast.makeText(this, "Vui lòng chọn lịch trống để đặt", Toast.LENGTH_SHORT).show();
        });
        
        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);
    }

    private void initViews() {
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate);
        tvAvailabilityStatus = findViewById(R.id.tvAvailabilityStatus);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        cardAvailabilityStatus = findViewById(R.id.cardAvailabilityStatus);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirm = findViewById(R.id.btnConfirmBooking);
    }

    private void bindRoomData() {
        tvRoomName.setText(room.getName());
        tvRoomPrice.setText(String.format("%,.0f VNĐ/đêm", room.getPrice()));
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);

            if (isCheckIn) {
                checkInCalendar = selected;
                tvCheckInDate.setText(sdf.format(selected.getTime()));
            } else {
                checkOutCalendar = selected;
                tvCheckOutDate.setText(sdf.format(selected.getTime()));
            }
            
            checkAvailabilityRealTime();
            calculateTotalPrice();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void checkAvailabilityRealTime() {
        if (checkInCalendar == null || checkOutCalendar == null) return;

        if (!checkOutCalendar.after(checkInCalendar)) {
            updateStatusUI(false, "Ngày trả phải sau ngày nhận");
            return;
        }

        updateStatusUI(null, "Đang kiểm tra lịch trống...");

        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("roomId", room.getId())
                .get()
                .addOnSuccessListener(snap -> {
                    boolean overlapped = false;
                    try {
                        long qStart = checkInCalendar.getTimeInMillis();
                        long qEnd = checkOutCalendar.getTimeInMillis();

                        for (QueryDocumentSnapshot doc : snap) {
                            String status = doc.getString("status");
                            if (Constants.STATUS_CANCELLED.equals(status)) continue;

                            Date bStartD = sdf.parse(doc.getString("checkIn"));
                            Date bEndD = sdf.parse(doc.getString("checkOut"));
                            
                            if (bStartD == null || bEndD == null) continue;
                            
                            // LOGIC CHUẨN: Hai khoảng thời gian (A, B) trùng nhau khi (StartA < EndB) và (EndA > StartB)
                            // Nếu qStart == bEndD (Khách này đến đúng lúc khách kia đi) -> KHÔNG trùng.
                            if (qStart < bEndD.getTime() && qEnd > bStartD.getTime()) {
                                overlapped = true;
                                break;
                            }
                        }
                    } catch (Exception ignored) {}

                    if (overlapped) updateStatusUI(false, "Lịch đã trùng. Vui lòng chọn ngày khác!");
                    else updateStatusUI(true, "Phòng hiện đang trống. Bạn có thể đặt!");
                });
    }

    private void updateStatusUI(Boolean available, String message) {
        tvAvailabilityStatus.setText(message);
        if (available == null) {
            btnConfirm.setEnabled(false); btnConfirm.setAlpha(0.5f); isAvailable = false;
        } else if (available) {
            cardAvailabilityStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            tvAvailabilityStatus.setTextColor(Color.parseColor("#2E7D32"));
            ivStatusIcon.setImageResource(android.R.drawable.checkbox_on_background);
            ivStatusIcon.setColorFilter(Color.parseColor("#2E7D32"));
            btnConfirm.setEnabled(true); btnConfirm.setAlpha(1.0f); isAvailable = true;
        } else {
            cardAvailabilityStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            tvAvailabilityStatus.setTextColor(Color.parseColor("#C62828"));
            ivStatusIcon.setImageResource(android.R.drawable.ic_delete);
            ivStatusIcon.setColorFilter(Color.parseColor("#C62828"));
            btnConfirm.setEnabled(false); btnConfirm.setAlpha(0.5f); isAvailable = false;
        }
    }

    private void calculateTotalPrice() {
        if (checkInCalendar != null && checkOutCalendar != null) {
            long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            if (days > 0) tvTotalPrice.setText(String.format("%,.0f VNĐ", days * room.getPrice()));
            else tvTotalPrice.setText("0 đ");
        }
    }

    private void performBooking() {
        String uid = FirebaseAuth.getInstance().getUid();
        String bookingId = db.collection(Constants.COLLECTION_BOOKINGS).document().getId();
        long days = (checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis()) / (24 * 60 * 60 * 1000);
        
        Booking booking = new Booking(bookingId, uid, room.getId(), room.getName(),
                sdf.format(checkInCalendar.getTime()), sdf.format(checkOutCalendar.getTime()),
                days * room.getPrice(), Constants.STATUS_PENDING);

        db.collection(Constants.COLLECTION_BOOKINGS).document(bookingId).set(booking)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đặt phòng thành công! Đang chờ duyệt.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}