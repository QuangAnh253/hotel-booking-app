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
import com.nhom4.hotelbooking.adapters.AdminParkAdapter;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminParkActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminParks;
    private AdminParkAdapter adapter;
    private List<Park> parkList = new ArrayList<>();
    private FirebaseFirestore db;
    private FloatingActionButton fabAddPark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_park);

        db = FirebaseFirestore.getInstance();
        recyclerAdminParks = findViewById(R.id.recyclerAdminParks);
        fabAddPark = findViewById(R.id.fabAddPark);

        recyclerAdminParks.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new AdminParkAdapter(parkList, new AdminParkAdapter.OnAdminParkActionListener() {
            @Override
            public void onEdit(Park park) {
                Intent intent = new Intent(AdminParkActivity.this, AdminAddEditParkActivity.class);
                intent.putExtra(Constants.EXTRA_PARK, park);
                startActivity(intent);
            }

            @Override
            public void onDelete(Park park) {
                new AlertDialog.Builder(AdminParkActivity.this)
                        .setTitle("Xoá khu vui chơi")
                        .setMessage("Bạn chắc chắn muốn xoá " + park.getName() + "?")
                        .setPositiveButton("Xoá", (d, w) -> deletePark(park))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        recyclerAdminParks.setAdapter(adapter);

        findViewById(R.id.btnBackAdminPark).setOnClickListener(v -> finish());

        fabAddPark.setOnClickListener(v -> {
            startActivity(new Intent(AdminParkActivity.this, AdminAddEditParkActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadParks();
    }

    private void loadParks() {
        db.collection(Constants.COLLECTION_PARKS).get()
                .addOnSuccessListener(snap -> {
                    parkList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Park p = doc.toObject(Park.class);
                        p.setId(doc.getId());
                        parkList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void deletePark(Park park) {
        db.collection(Constants.COLLECTION_PARKS).document(park.getId()).delete()
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show();
                    loadParks();
                });
    }
}