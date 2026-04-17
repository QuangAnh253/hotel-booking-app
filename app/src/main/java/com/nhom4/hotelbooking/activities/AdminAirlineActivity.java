package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminAirlineAdapter;
import com.nhom4.hotelbooking.models.Airline;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminAirlineActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminAirlines;
    private AdminAirlineAdapter adapter;
    private List<Airline> airlineList = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddAirline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_airline);

        db = FirebaseFirestore.getInstance();
        recyclerAdminAirlines = findViewById(R.id.recyclerAdminAirlines);
        fabAddAirline = findViewById(R.id.fabAddAirline);

        recyclerAdminAirlines.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminAirlineAdapter(airlineList, new AdminAirlineAdapter.OnAirlineActionListener() {
            @Override
            public void onEdit(Airline airline) {
                Intent intent = new Intent(AdminAirlineActivity.this, AdminAddEditAirlineActivity.class);
                intent.putExtra(Constants.EXTRA_AIRLINE, airline);
                startActivity(intent);
            }

            @Override
            public void onDelete(Airline airline) {
                new AlertDialog.Builder(AdminAirlineActivity.this)
                        .setTitle("Xoá hãng hàng không")
                        .setMessage("Bạn chắc chắn muốn xoá " + airline.getName() + "?")
                        .setPositiveButton("Xoá", (d, w) -> deleteAirline(airline))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        recyclerAdminAirlines.setAdapter(adapter);

        findViewById(R.id.btnBackAdminAirline).setOnClickListener(v -> finish());

        fabAddAirline.setOnClickListener(v -> {
            startActivity(new Intent(AdminAirlineActivity.this, AdminAddEditAirlineActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAirlines();
    }

    private void loadAirlines() {
        db.collection(Constants.COLLECTION_AIRLINES).get()
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

    private void deleteAirline(Airline airline) {
        db.collection(Constants.COLLECTION_AIRLINES).document(airline.getId()).delete()
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show();
                    loadAirlines();
                });
    }
}