package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.database.DatabaseHelper;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    TextView tvRoomName, tvRoomPrice, tvTotalPrice, tvBookedDates;
    DatePicker datePickerCheckIn, datePickerCheckOut;
    Button btnConfirmBooking;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseHelper dbHelper;

    Room room;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Lưu danh sách khoảng ngày đã đặt để kiểm tra trùng
    List<long[]> bookedRanges = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvBookedDates = findViewById(R.id.tvBookedDates);
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

        // Load ngày đã đặt trước khi cho chọn
        loadBookedDates();

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

    void loadBookedDates() {
        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("roomId", room.getId())
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    bookedRanges.clear();
                    StringBuilder sb = new StringBuilder();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String status = doc.getString("status");

                        // Chỉ chặn booking pending hoặc confirmed, bỏ qua cancelled
                        if (status == null || status.equals(Constants.STATUS_CANCELLED)) continue;

                        String checkIn = doc.getString("checkIn");
                        String checkOut = doc.getString("checkOut");

                        try {
                            Date inDate = sdf.parse(checkIn);
                            Date outDate = sdf.parse(checkOut);
                            bookedRanges.add(new long[]{inDate.getTime(), outDate.getTime()});
                            sb.append("• ").append(checkIn).append(" → ").append(checkOut).append("\n");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (bookedRanges.isEmpty()) {
                        tvBookedDates.setText("Phòng chưa có lịch đặt nào");
                    } else {
                        tvBookedDates.setText(sb.toString().trim());
                    }
                });
    }

    void tinhTien() {
        Calendar checkIn = getCalendarFromPicker(datePickerCheckIn);
        Calendar checkOut = getCalendarFromPicker(datePickerCheckOut);

        long diff = checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
        long soNgay = diff / (1000 * 60 * 60 * 24);

        if (soNgay <= 0) {
            tvTotalPrice.setText("Check-out phải sau Check-in");
            return;
        }

        double tong = soNgay * room.getPrice();
        tvTotalPrice.setText(String.format("Tổng tiền: %,.0f VNĐ (%d đêm)", tong, soNgay));
    }

    boolean isTrungLich(long newIn, long newOut) {
        for (long[] range : bookedRanges) {
            long existIn = range[0];
            long existOut = range[1];
            // Trùng lịch nếu: newIn < existOut VÀ newOut > existIn
            if (newIn < existOut && newOut > existIn) {
                return true;
            }
        }
        return false;
    }

    void datPhong() {
        Calendar checkIn = getCalendarFromPicker(datePickerCheckIn);
        Calendar checkOut = getCalendarFromPicker(datePickerCheckOut);

        long diff = checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
        long soNgay = diff / (1000 * 60 * 60 * 24);

        if (soNgay <= 0) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra trùng lịch
        if (isTrungLich(checkIn.getTimeInMillis(), checkOut.getTimeInMillis())) {
            Toast.makeText(this,
                    "Phòng đã có người đặt trong khoảng thời gian này!\nVui lòng chọn ngày khác.",
                    Toast.LENGTH_LONG).show();
            return;
        }

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
                    Booking booking = new Booking(
                            docRef.getId(), uid, room.getId(),
                            room.getName(), checkInStr, checkOutStr,
                            tongTien, Constants.STATUS_PENDING
                    );
                    dbHelper.insertBooking(booking);

                    // Không đổi status phòng nữa — phòng luôn available
                    // Chỉ chặn theo ngày

                    setResult(RESULT_OK);
                    Toast.makeText(this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    Calendar getCalendarFromPicker(DatePicker picker) {
        Calendar cal = Calendar.getInstance();
        cal.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth(), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}