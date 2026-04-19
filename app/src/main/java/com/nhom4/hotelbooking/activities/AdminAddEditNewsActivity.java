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
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AdminAddEditNewsActivity extends AppCompatActivity {

    private TextInputEditText edtNewsTitle, edtNewsDesc, edtNewsContent;
    private ImageView imgNewsSelect;
    private MaterialButton btnSaveNews;
    private FrameLayout layoutLoading;
    private TextView tvAddEditNewsTitle;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private News existingNews;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(imgNewsSelect);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_news);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();

        existingNews = (News) getIntent().getSerializableExtra(Constants.EXTRA_NEWS);
        if (existingNews != null) {
            setupEditMode();
        }

        imgNewsSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnSaveNews.setOnClickListener(v -> validateAndSave());
        findViewById(R.id.btnBackAddNews).setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtNewsTitle = findViewById(R.id.edtNewsTitle);
        edtNewsDesc = findViewById(R.id.edtNewsDesc);
        edtNewsContent = findViewById(R.id.edtNewsContent);
        imgNewsSelect = findViewById(R.id.imgNewsSelect);
        btnSaveNews = findViewById(R.id.btnSaveNews);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvAddEditNewsTitle = findViewById(R.id.tvAddEditNewsTitle);
    }

    private void setupEditMode() {
        tvAddEditNewsTitle.setText("Chỉnh sửa Tin tức");
        edtNewsTitle.setText(existingNews.getTitle());
        edtNewsDesc.setText(existingNews.getDescription());
        edtNewsContent.setText(existingNews.getContent());
        Glide.with(this).load(existingNews.getImageUrl()).into(imgNewsSelect);
    }

    private void validateAndSave() {
        String title = edtNewsTitle.getText().toString().trim();
        String desc = edtNewsDesc.getText().toString().trim();
        String content = edtNewsContent.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingNews == null && imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutLoading.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            uploadImageAndSave(title, desc, content);
        } else {
            saveToFirestore(title, desc, content, existingNews.getImageUrl());
        }
    }

    private void uploadImageAndSave(String title, String desc, String content) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("news_images/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(title, desc, content, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String title, String desc, String content, String imageUrl) {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        News news = new News(title, desc, imageUrl, date);
        
        if (existingNews == null) {
            db.collection(Constants.COLLECTION_NEWS).add(news)
                    .addOnSuccessListener(documentReference -> {
                        documentReference.update("content", content);
                        finishWithSuccess("Đã thêm tin tức");
                    });
        } else {
            db.collection(Constants.COLLECTION_NEWS).document(existingNews.getId()).set(news)
                    .addOnSuccessListener(unused -> {
                        db.collection(Constants.COLLECTION_NEWS).document(existingNews.getId()).update("content", content);
                        finishWithSuccess("Đã cập nhật tin tức");
                    });
        }
    }

    private void finishWithSuccess(String message) {
        layoutLoading.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}