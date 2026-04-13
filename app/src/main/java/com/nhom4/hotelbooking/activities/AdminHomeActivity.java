package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminBookingAdapter;
import com.nhom4.hotelbooking.adapters.AdminRoomAdapter;
import com.nhom4.hotelbooking.database.DatabaseHelper;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    Toolbar toolbarAdmin;
    TabLayout tabLayout;
    FloatingActionButton fabAddRoom;
    RecyclerView recyclerAdminRooms;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseHelper dbHelper;

    AdminRoomAdapter roomAdapter;
    AdminBookingAdapter bookingAdapter;

    List<Room> roomList;
    List<Booking> bookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        toolbarAdmin = findViewById(R.id.toolbarAdmin);
        tabLayout = findViewById(R.id.tabLayout);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        setSupportActionBar(toolbarAdmin);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản trị");
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        roomList = new ArrayList<>();
        bookingList = new ArrayList<>();

        recyclerAdminRooms = new RecyclerView(this);
        recyclerAdminRooms.setLayoutManager(new LinearLayoutManager(this));

        roomAdapter = new AdminRoomAdapter(roomList, new AdminRoomAdapter.OnAdminRoomActionListener() {
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
        recyclerAdminRooms.setAdapter(roomAdapter);
        ((android.widget.FrameLayout) findViewById(R.id.frameAdmin)).addView(recyclerAdminRooms);

        tabLayout.addTab(tabLayout.newTab().setText("Phòng"));
        tabLayout.addTab(tabLayout.newTab().setText("Booking"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    fabAddRoom.show();
                    loadRooms();
                } else {
                    fabAddRoom.hide();
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
        if (tabLayout.getSelectedTabPosition() == 0) {
            loadRooms();
        }
    }

    void loadRooms() {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();
        recyclerAdminRooms = new RecyclerView(this);
        recyclerAdminRooms.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdminRooms.setAdapter(roomAdapter);
        frame.addView(recyclerAdminRooms);

        db.collection(Constants.COLLECTION_ROOMS).get()
                .addOnSuccessListener(querySnapshots -> {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Room room = doc.toObject(Room.class);
                        room.setId(doc.getId());
                        roomList.add(room);
                    }
                    roomAdapter.notifyDataSetChanged();
                });
    }

    void loadBookings() {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();

        RecyclerView recyclerBookings = new RecyclerView(this);
        recyclerBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingAdapter = new AdminBookingAdapter(bookingList,
                new AdminBookingAdapter.OnAdminBookingActionListener() {
                    @Override
                    public void onApprove(Booking booking) {
                        updateBookingStatus(booking, Constants.STATUS_CONFIRMED);
                    }

                    @Override
                    public void onReject(Booking booking) {
                        updateBookingStatus(booking, Constants.STATUS_CANCELLED);
                    }
                });

        recyclerBookings.setAdapter(bookingAdapter);
        frame.addView(recyclerBookings);

        db.collection(Constants.COLLECTION_BOOKINGS).get()
                .addOnSuccessListener(querySnapshots -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        bookingList.add(booking);
                    }
                    bookingAdapter.notifyDataSetChanged();
                });
    }

    void updateBookingStatus(Booking booking, String newStatus) {
        db.collection(Constants.COLLECTION_BOOKINGS).document(booking.getId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    dbHelper.updateBookingStatus(booking.getId(), newStatus);
                    booking.setStatus(newStatus);
                    bookingAdapter.notifyDataSetChanged();
                    String msg = newStatus.equals(Constants.STATUS_CONFIRMED) ? "Đã duyệt booking" : "Đã huỷ booking";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    void deleteRoom(Room room) {
        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("roomId", room.getId())
                .whereEqualTo("status", Constants.STATUS_PENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        Toast.makeText(this,
                                "Không thể xoá! Phòng đang có " + querySnapshots.size() + " booking pending",
                                Toast.LENGTH_LONG).show();
                    } else {
                        db.collection(Constants.COLLECTION_ROOMS).document(room.getId()).delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đã xoá phòng", Toast.LENGTH_SHORT).show();
                                    loadRooms();
                                });
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            if (tabLayout.getSelectedTabPosition() == 0) {
                loadRooms();
            } else {
                loadBookings();
            }
            Toast.makeText(this, "Đã làm mới", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }
}