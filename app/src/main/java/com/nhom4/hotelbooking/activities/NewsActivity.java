package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.NewsAdapter;
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {
    private List<News> newsList = new ArrayList<>();
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        findViewById(R.id.btnBackNews).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerNews);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(newsList, false); // false = vertical
        recycler.setAdapter(adapter);

        loadAllNews();
    }

    private void loadAllNews() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NEWS)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    newsList.clear();
                    for (QueryDocumentSnapshot doc : snap) newsList.add(doc.toObject(News.class));
                    adapter.notifyDataSetChanged();
                });
    }
}