package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.UUID;

public class AdminAddEditParkActivity extends AppCompatActivity {

    private TextInputEditText edtParkName, edtParkPrice, edtParkDesc;
    private ImageView imgParkSelect;
    private MaterialButton btnSavePark;
    private FrameLayout layoutLoading;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Park existingPark;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imgParkSelect.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_park);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();

        existingPark = (Park) getIntent().getSerializableExtra(Constants.EXTRA_PARK);
        if (existingPark != null) {
            setupEditMode();
        }

        imgParkSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSavePark.setOnClickListener(v -> validateAndSave());
        findViewById(R.id.btnBackAddPark).setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtParkName = findViewById(R.id.edtParkName);
        edtParkPrice = findViewById(R.id.edtParkPrice);
        edtParkDesc = findViewById(R.id.edtParkDesc);
        imgParkSelect = findViewById(R.id.imgParkSelect);
        btnSavePark = findViewById(R.id.btnSavePark);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvTitle = findViewById(R.id.tvAddEditParkTitle);
    }

    private void setupEditMode() {
        tvTitle.setText("Chỉnh sửa Khu vui chơi");
        edtParkName.setText(existingPark.getName());
        edtParkPrice.setText(String.valueOf((long) existingPark.getPrice()));
        edtParkDesc.setText(existingPark.getDescription());
        Glide.with(this).load(existingPark.getImageUrl()).into(imgParkSelect);
    }

    private void validateAndSave() {
        String name = edtParkName.getText().toString().trim();
        String priceStr = edtParkPrice.getText().toString().trim();
        String desc = edtParkDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingPark == null && imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        layoutLoading.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            uploadImageAndSave(name, price, desc);
        } else {
            saveToFirestore(name, price, desc, existingPark.getImageUrl());
        }
    }

    private void uploadImageAndSave(String name, double price, String desc) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("park_images/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(name, price, desc, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String name, double price, String desc, String imageUrl) {
        Park park = new Park(name, desc, imageUrl, price);
        
        if (existingPark == null) {
            db.collection(Constants.COLLECTION_PARKS).add(park)
                    .addOnSuccessListener(doc -> finishWithSuccess("Đã thêm khu vui chơi"));
        } else {
            db.collection(Constants.COLLECTION_PARKS).document(existingPark.getId()).set(park)
                    .addOnSuccessListener(unused -> finishWithSuccess("Đã cập nhật"));
        }
    }

    private void finishWithSuccess(String msg) {
        layoutLoading.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}