package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.ParkAdapter;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class ParkActivity extends AppCompatActivity {
    private List<Park> parkList = new ArrayList<>();
    private ParkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        findViewById(R.id.btnBackPark).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerParks);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParkAdapter(parkList);
        recycler.setAdapter(adapter);

        loadAllParks();
    }

    private void loadAllParks() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PARKS).get()
                .addOnSuccessListener(snap -> {
                    parkList.clear();
                    for (QueryDocumentSnapshot doc : snap) parkList.add(doc.toObject(Park.class));
                    adapter.notifyDataSetChanged();
                });
    }
}