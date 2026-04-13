package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminRoomAdapter;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    FloatingActionButton fabAddRoom;
    RecyclerView recyclerAdminRooms;

    FirebaseFirestore db;
    AdminRoomAdapter adapter;
    List<Room> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        tabLayout = findViewById(R.id.tabLayout);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        db = FirebaseFirestore.getInstance();
        roomList = new ArrayList<>();

        // Tạo RecyclerView động và gắn vào frameAdmin
        recyclerAdminRooms = new RecyclerView(this);
        recyclerAdminRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRoomAdapter(roomList, new AdminRoomAdapter.OnAdminRoomActionListener() {
            @Override
            public void onEdit(Room room) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminAddEditRoomActivity.class);
                intent.putExtra(Constants.EXTRA_ROOM, room);
                startActivity(intent);
            }

            @Override
            public void onDelete(Room room) {
                new AlertDialog.Builder(AdminHomeActivity.this)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn có chắc muốn xoá phòng " + room.getName() + "?")
                        .setPositiveButton("Xoá", (dialog, which) -> deleteRoom(room))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });
        recyclerAdminRooms.setAdapter(adapter);
        ((android.widget.FrameLayout) findViewById(R.id.frameAdmin)).addView(recyclerAdminRooms);

        tabLayout.addTab(tabLayout.newTab().setText("Phòng"));
        tabLayout.addTab(tabLayout.newTab().setText("Booking"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadRooms();
                } else {
                    loadBookings();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddRoom.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, AdminAddEditRoomActivity.class));
        });

        loadRooms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }

    void loadRooms() {
        db.collection(Constants.COLLECTION_ROOMS).get()
                .addOnSuccessListener(querySnapshots -> {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Room room = doc.toObject(Room.class);
                        room.setId(doc.getId());
                        roomList.add(room);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    void loadBookings() {
        // Chuyển RecyclerView sang hiển thị Booking — đơn giản: dùng Toast báo
        Toast.makeText(this, "Tab Booking: xem AdminBookingAdapter để mở rộng", Toast.LENGTH_SHORT).show();
        // (Hướng dẫn mở rộng: tạo AdminBookingAdapter tương tự BookingAdapter nhưng có nút Duyệt/Huỷ)
    }

    void deleteRoom(Room room) {
        db.collection(Constants.COLLECTION_ROOMS).document(room.getId()).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xoá phòng", Toast.LENGTH_SHORT).show();
                    loadRooms();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Làm mới");
        menu.add(0, 2, 0, "Đăng xuất");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            loadRooms();
            Toast.makeText(this, "Đã làm mới", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == 2) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }
}
