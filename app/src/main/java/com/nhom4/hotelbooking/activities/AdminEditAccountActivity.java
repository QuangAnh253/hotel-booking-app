package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Review;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdminEditAccountActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtName, edtPhone;
    private RadioGroup rgRole;
    private RadioButton rbUser, rbAdmin;
    private MaterialButton btnUpdate, btnFakeData;
    
    private FirebaseFirestore db;
    private User targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_account);

        db = FirebaseFirestore.getInstance();
        targetUser = (User) getIntent().getSerializableExtra("user");

        if (targetUser == null) {
            finish();
            return;
        }

        initViews();
        bindData();

        findViewById(R.id.btnBackEditAccount).setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateAccount());
        btnFakeData.setOnClickListener(v -> generateFakeDataForUser());
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEditAccountEmail);
        edtName = findViewById(R.id.edtEditAccountName);
        edtPhone = findViewById(R.id.edtEditAccountPhone);
        rgRole = findViewById(R.id.rgRole);
        rbUser = findViewById(R.id.rbUser);
        rbAdmin = findViewById(R.id.rbAdmin);
        btnUpdate = findViewById(R.id.btnUpdateAccount);
        btnFakeData = findViewById(R.id.btnFakeData);
    }

    private void bindData() {
        edtEmail.setText(targetUser.getEmail());
        edtName.setText(targetUser.getName());
        edtPhone.setText(targetUser.getPhone());
        if (Constants.ROLE_ADMIN.equals(targetUser.getRole())) rbAdmin.setChecked(true);
        else rbUser.setChecked(true);
    }

    private void generateFakeDataForUser() {
        Toast.makeText(this, "Đang tạo dữ liệu mồi...", Toast.LENGTH_SHORT).show();
        
        db.collection(Constants.COLLECTION_ROOMS).get().addOnSuccessListener(snap -> {
            if (snap.isEmpty()) return;
            
            List<Room> allRooms = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                Room r = doc.toObject(Room.class);
                r.setId(doc.getId());
                allRooms.add(r);
            }

            Random r = new Random();
            String[] comments = {
                "Phòng rất sạch sẽ và sang trọng, tôi sẽ quay lại!",
                "Dịch vụ tuyệt vời, nhân viên rất nhiệt tình chu đáo.",
                "View phòng cực đẹp, không gian yên tĩnh thoải mái.",
                "Giá cả hợp lý so với chất lượng, trang thiết bị hiện đại.",
                "Vị trí thuận tiện, buffet sáng rất ngon miệng."
            };

            for (int i = 0; i < 3; i++) {
                Room randomRoom = allRooms.get(r.nextInt(allRooms.size()));
                
                String bookingId = "FAKE_" + System.currentTimeMillis() + "_" + i;
                Booking b = new Booking(bookingId, targetUser.getUid(), randomRoom.getId(), 
                                       randomRoom.getName(), "10/04/2024", "12/04/2024", 
                                       randomRoom.getPrice() * 2, Constants.STATUS_CONFIRMED);
                db.collection(Constants.COLLECTION_BOOKINGS).document(bookingId).set(b);

                // Làm tròn số sao (4.0, 4.5, 5.0) để trông thật hơn
                float[] stars = {4.0f, 4.5f, 5.0f};
                Review rv = new Review(
                    targetUser.getUid(),
                    targetUser.getName(),
                    randomRoom.getId(),
                    stars[r.nextInt(stars.length)],
                    comments[r.nextInt(comments.length)],
                    System.currentTimeMillis() - (long)i * 3600000 // cách nhau 1 tiếng
                );
                db.collection(Constants.COLLECTION_REVIEWS).add(rv);
            }

            Toast.makeText(this, "Đã tạo 3 lịch sử & review thật 100%", Toast.LENGTH_LONG).show();
        });
    }

    private void updateAccount() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String role = rbAdmin.isChecked() ? Constants.ROLE_ADMIN : Constants.ROLE_USER;

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("role", role);

        db.collection(Constants.COLLECTION_USERS).document(targetUser.getUid()).update(updates)
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}