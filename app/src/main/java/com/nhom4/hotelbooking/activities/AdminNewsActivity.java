package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminNewsAdapter;
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminNewsActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminNews;
    private AdminNewsAdapter adapter;
    private List<News> newsList = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_news);

        db = FirebaseFirestore.getInstance();
        recyclerAdminNews = findViewById(R.id.recyclerAdminNews);
        fabAddNews = findViewById(R.id.fabAddNews);
        
        recyclerAdminNews.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminNewsAdapter(newsList, new AdminNewsAdapter.OnAdminNewsActionListener() {
            @Override
            public void onEdit(News news) {
                Intent intent = new Intent(AdminNewsActivity.this, AdminAddEditNewsActivity.class);
                intent.putExtra(Constants.EXTRA_NEWS, news);
                startActivity(intent);
            }

            @Override
            public void onDelete(News news) {
                new AlertDialog.Builder(AdminNewsActivity.this)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn có chắc muốn xoá bài viết này?")
                        .setPositiveButton("Xoá", (d, w) -> deleteNews(news))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        recyclerAdminNews.setAdapter(adapter);

        findViewById(R.id.btnBackAdminNews).setOnClickListener(v -> finish());
        
        // Gán sự kiện cho nút Thêm mới
        if (fabAddNews != null) {
            fabAddNews.setOnClickListener(v -> {
                Intent intent = new Intent(AdminNewsActivity.this, AdminAddEditNewsActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNews();
    }

    private void loadNews() {
        db.collection(Constants.COLLECTION_NEWS)
                .get()
                .addOnSuccessListener(snap -> {
                    newsList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        News n = doc.toObject(News.class);
                        n.setId(doc.getId());
                        newsList.add(n);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải tin tức", Toast.LENGTH_SHORT).show());
    }

    private void deleteNews(News news) {
        db.collection(Constants.COLLECTION_NEWS).document(news.getId())
                .delete()
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Đã xoá bài viết", Toast.LENGTH_SHORT).show();
                    loadNews();
                });
    }
}