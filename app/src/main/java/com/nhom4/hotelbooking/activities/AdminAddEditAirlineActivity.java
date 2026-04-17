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
import com.nhom4.hotelbooking.models.Airline;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.UUID;

public class AdminAddEditAirlineActivity extends AppCompatActivity {

    private TextInputEditText edtAirlineName, edtAirlineUrl;
    private ImageView imgAirlineSelect;
    private MaterialButton btnSaveAirline;
    private FrameLayout layoutLoading;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Airline existingAirline;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(imgAirlineSelect);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_airline);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();

        existingAirline = (Airline) getIntent().getSerializableExtra(Constants.EXTRA_AIRLINE);
        if (existingAirline != null) {
            setupEditMode();
        }

        imgAirlineSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSaveAirline.setOnClickListener(v -> validateAndSave());
        findViewById(R.id.btnBackAddAirline).setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtAirlineName = findViewById(R.id.edtAirlineName);
        edtAirlineUrl = findViewById(R.id.edtAirlineUrl);
        imgAirlineSelect = findViewById(R.id.imgAirlineSelect);
        btnSaveAirline = findViewById(R.id.btnSaveAirline);
        layoutLoading = findViewById(R.id.layoutLoadingAirline);
        tvTitle = findViewById(R.id.tvAddEditAirlineTitle);
    }

    private void setupEditMode() {
        tvTitle.setText("Chỉnh sửa Hãng");
        edtAirlineName.setText(existingAirline.getName());
        edtAirlineUrl.setText(existingAirline.getWebUrl());
        Glide.with(this).load(existingAirline.getLogoUrl()).into(imgAirlineSelect);
    }

    private void validateAndSave() {
        String name = edtAirlineName.getText().toString().trim();
        String url = edtAirlineUrl.getText().toString().trim();

        if (name.isEmpty() || url.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingAirline == null && imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn logo hãng", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutLoading.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            uploadImageAndSave(name, url);
        } else {
            saveToFirestore(name, url, existingAirline.getLogoUrl());
        }
    }

    private void uploadImageAndSave(String name, String url) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("airline_logos/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(name, url, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String name, String url, String logoUrl) {
        Airline airline = new Airline(name, logoUrl, url);
        
        if (existingAirline == null) {
            db.collection(Constants.COLLECTION_AIRLINES).add(airline)
                    .addOnSuccessListener(doc -> finishWithSuccess("Đã thêm hãng hàng không"))
                    .addOnFailureListener(e -> {
                        layoutLoading.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection(Constants.COLLECTION_AIRLINES).document(existingAirline.getId()).set(airline)
                    .addOnSuccessListener(unused -> finishWithSuccess("Đã cập nhật"))
                    .addOnFailureListener(e -> {
                        layoutLoading.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void finishWithSuccess(String msg) {
        layoutLoading.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }
}