package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AirlineGuestAdapter;
import com.nhom4.hotelbooking.models.Airline;
import com.nhom4.hotelbooking.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class AirlineGuestActivity extends AppCompatActivity {
    private List<Airline> airlineList = new ArrayList<>();
    private AirlineGuestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airline_guest);

        findViewById(R.id.btnBackAirlineGuest).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerAirlineGuest);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AirlineGuestAdapter(airlineList, airline -> {
            // MỞ WEBVIEW NỘI BỘ THAY VÌ TRÌNH DUYỆT MÁY
            Intent intent = new Intent(AirlineGuestActivity.this, AirlineWebViewActivity.class);
            intent.putExtra("url", airline.getWebUrl());
            intent.putExtra("name", airline.getName());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        loadAirlines();
    }

    private void loadAirlines() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_AIRLINES).get()
                .addOnSuccessListener(snap -> {
                    airlineList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Airline a = doc.toObject(Airline.class);
                        a.setId(doc.getId());
                        airlineList.add(a);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}