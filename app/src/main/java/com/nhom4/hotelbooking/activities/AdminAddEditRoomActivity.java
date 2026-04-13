package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminAddEditRoomActivity extends AppCompatActivity {

    TextView tvAdminFormTitle;
    EditText edtRoomName, edtRoomType, edtRoomPrice, edtRoomDescription, edtRoomCapacity;
    Button btnPickImage, btnSaveRoom;
    ImageView imgPreview;

    FirebaseFirestore db;
    FirebaseStorage storage;

    Uri selectedImageUri = null;
    Room existingRoom = null; // null = thêm mới, khác null = sửa

    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_room);

        tvAdminFormTitle = findViewById(R.id.tvAdminFormTitle);
        edtRoomName = findViewById(R.id.edtRoomName);
        edtRoomType = findViewById(R.id.edtRoomType);
        edtRoomPrice = findViewById(R.id.edtRoomPrice);
        edtRoomDescription = findViewById(R.id.edtRoomDescription);
        edtRoomCapacity = findViewById(R.id.edtRoomCapacity);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSaveRoom = findViewById(R.id.btnSaveRoom);
        imgPreview = findViewById(R.id.imgPreview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Launcher để chọn ảnh từ thư viện
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgPreview.setImageURI(uri);
                    }
                }
        );

        // Kiểm tra đang thêm hay sửa
        existingRoom = (Room) getIntent().getSerializableExtra(Constants.EXTRA_ROOM);
        if (existingRoom != null) {
            tvAdminFormTitle.setText("Sửa phòng");
            edtRoomName.setText(existingRoom.getName());
            edtRoomType.setText(existingRoom.getType());
            edtRoomPrice.setText(String.valueOf(existingRoom.getPrice()));
            edtRoomDescription.setText(existingRoom.getDescription());
            edtRoomCapacity.setText(String.valueOf(existingRoom.getCapacity()));
        }

        btnPickImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSaveRoom.setOnClickListener(v -> saveRoom());
    }

    void saveRoom() {
        String name = edtRoomName.getText().toString().trim();
        String type = edtRoomType.getText().toString().trim();
        String priceStr = edtRoomPrice.getText().toString().trim();
        String description = edtRoomDescription.getText().toString().trim();

        int capacity = 0;
        String capacityStr = edtRoomCapacity.getText().toString().trim();
        if (!capacityStr.isEmpty()) {
            try {
                capacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Sức chứa phải là số", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (name.isEmpty() || type.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên, loại, giá", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            // Có chọn ảnh mới → upload lên Storage trước
            uploadImageThenSave(name, type, price, description, capacity);
        } else {
            // Không chọn ảnh → lưu thẳng (giữ URL cũ nếu đang sửa)
            String imageUrl = (existingRoom != null) ? existingRoom.getImageUrl() : "";
            saveToFirestore(name, type, price, description, imageUrl, capacity);
        }
    }

    void uploadImageThenSave(String name, String type, double price, String description, int capacity) {
        Toast.makeText(this, "Đang upload ảnh...", Toast.LENGTH_SHORT).show();

        String fileName = "rooms/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        // 👉 truyền thêm capacity vào đây
                        saveToFirestore(name, type, price, description, uri.toString(), capacity);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    void saveToFirestore(String name, String type, double price, String description, String imageUrl, int capacity) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("name", name);
        roomData.put("type", type);
        roomData.put("price", price);
        roomData.put("description", description);
        roomData.put("imageUrl", imageUrl);
        roomData.put("status", Constants.STATUS_AVAILABLE);
        roomData.put("capacity", capacity);

        if (existingRoom == null) {
            // Thêm mới
            db.collection(Constants.COLLECTION_ROOMS).add(roomData)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Cập nhật
            db.collection(Constants.COLLECTION_ROOMS).document(existingRoom.getId()).update(roomData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Cập nhật phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}
