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
import com.nhom4.hotelbooking.adapters.ChatConversationAdapter;
import com.nhom4.hotelbooking.database.DatabaseHelper;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity {

    Toolbar toolbarAdmin;
    TabLayout tabLayout;
    FloatingActionButton fabAddRoom;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseHelper dbHelper;

    AdminRoomAdapter roomAdapter;
    AdminBookingAdapter bookingAdapter;
    ChatConversationAdapter chatAdapter;

    List<Room> roomList;
    List<Booking> bookingList;
    List<Map<String, Object>> conversationList;

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
        conversationList = new ArrayList<>();

        tabLayout.addTab(tabLayout.newTab().setText("Phòng"));
        tabLayout.addTab(tabLayout.newTab().setText("Booking"));
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    fabAddRoom.show();
                    loadRooms();
                } else if (tab.getPosition() == 1) {
                    fabAddRoom.hide();
                    loadBookings();
                } else if (tab.getPosition() == 2) {
                    fabAddRoom.hide();
                    loadChats();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddRoom.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAddEditRoomActivity.class))
        );

        loadRooms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int pos = tabLayout.getSelectedTabPosition();
        if (pos == 0) loadRooms();
        else if (pos == 1) loadBookings();
        else if (pos == 2) loadChats();
    }

    // ================= ROOMS =================
    void loadRooms() {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();

        RecyclerView recycler = new RecyclerView(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));

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
                        .setPositiveButton("Xoá", (d, w) -> deleteRoom(room))
                        .setNegativeButton("Huỷ", null)
                        .show();
            }
        });

        recycler.setAdapter(roomAdapter);
        frame.addView(recycler);

        db.collection(Constants.COLLECTION_ROOMS).get()
                .addOnSuccessListener(snap -> {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Room r = doc.toObject(Room.class);
                        r.setId(doc.getId());
                        roomList.add(r);
                    }
                    roomAdapter.notifyDataSetChanged();
                });
    }

    // ================= BOOKINGS =================
    void loadBookings() {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();

        RecyclerView recycler = new RecyclerView(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));

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

        recycler.setAdapter(bookingAdapter);
        frame.addView(recycler);

        db.collection(Constants.COLLECTION_BOOKINGS).get()
                .addOnSuccessListener(snap -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Booking b = doc.toObject(Booking.class);
                        b.setId(doc.getId());
                        bookingList.add(b);
                    }
                    bookingAdapter.notifyDataSetChanged();
                });
    }

    // ================= CHATS =================
    void loadChats() {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();

        conversationList = new ArrayList<>();

        RecyclerView recyclerChats = new RecyclerView(this);
        recyclerChats.setLayoutManager(new LinearLayoutManager(this));

        chatAdapter = new ChatConversationAdapter(conversationList,
                (userId, userName) -> {
                    Intent intent = new Intent(this, AdminChatDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_USER_ID, userId);
                    intent.putExtra(Constants.EXTRA_USER_NAME, userName);
                    startActivity(intent);
                });

        recyclerChats.setAdapter(chatAdapter);
        frame.addView(recyclerChats);

        db.collection(Constants.COLLECTION_CHATS)
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    conversationList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        conversationList.add(doc.getData());
                    }
                    chatAdapter.notifyDataSetChanged();
                });
    }

    // ================= HELPERS =================
    void updateBookingStatus(Booking booking, String newStatus) {
        db.collection(Constants.COLLECTION_BOOKINGS)
                .document(booking.getId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    dbHelper.updateBookingStatus(booking.getId(), newStatus);
                    booking.setStatus(newStatus);
                    bookingAdapter.notifyDataSetChanged();
                    String msg = newStatus.equals(Constants.STATUS_CONFIRMED)
                            ? "Đã duyệt booking" : "Đã huỷ booking";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    void deleteRoom(Room room) {
        // Kiểm tra còn booking pending không trước khi xoá
        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("roomId", room.getId())
                .whereEqualTo("status", Constants.STATUS_PENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        Toast.makeText(this,
                                "Không thể xoá! Phòng đang có " + snap.size() + " booking pending",
                                Toast.LENGTH_LONG).show();
                    } else {
                        db.collection(Constants.COLLECTION_ROOMS)
                                .document(room.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đã xoá phòng", Toast.LENGTH_SHORT).show();
                                    loadRooms();
                                });
                    }
                });
    }

    // ================= MENU (Làm mới + Đăng xuất) =================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            int pos = tabLayout.getSelectedTabPosition();
            if (pos == 0) loadRooms();
            else if (pos == 1) loadBookings();
            else loadChats();
            Toast.makeText(this, "Đã làm mới", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }
}