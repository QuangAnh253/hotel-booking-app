package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminBookingAdapter;
import com.nhom4.hotelbooking.adapters.AdminRoomAdapter;
import com.nhom4.hotelbooking.adapters.ChatConversationAdapter;
import com.nhom4.hotelbooking.fragments.AdminDashboardFragment;
import com.nhom4.hotelbooking.fragments.AdminMoreFragment;
import com.nhom4.hotelbooking.fragments.ProfileFragment;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity {

    Toolbar toolbarAdmin;
    TextView tvAdminTitle;
    ImageButton btnAdminBack;
    BottomNavigationView adminBottomNav;
    FloatingActionButton fabAddRoom;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    AdminRoomAdapter roomAdapter;
    AdminBookingAdapter bookingAdapter;
    ChatConversationAdapter chatAdapter;

    List<Room> roomList = new ArrayList<>();
    List<Booking> bookingList = new ArrayList<>();
    List<Map<String, Object>> conversationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        toolbarAdmin = findViewById(R.id.toolbarAdmin);
        tvAdminTitle = findViewById(R.id.tvAdminTitle);
        btnAdminBack = findViewById(R.id.btnAdminBack);
        adminBottomNav = findViewById(R.id.adminBottomNav);
        fabAddRoom = findViewById(R.id.fabAddRoom);

        setSupportActionBar(toolbarAdmin);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnAdminBack.setOnClickListener(v -> showDashboard());

        adminBottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_home) {
                showDashboard();
                return true;
            } else if (id == R.id.admin_nav_chat) {
                loadAdminChats();
                return true;
            } else if (id == R.id.admin_nav_accounts) {
                updateHeader("Hồ sơ cá nhân");
                btnAdminBack.setVisibility(View.GONE);
                loadFragment(new ProfileFragment());
                fabAddRoom.hide();
                return true;
            } else if (id == R.id.admin_nav_more) {
                // Đã sửa: Tab "Thêm" trỏ về AdminMoreFragment thay vì hiện Logout
                updateHeader("Thêm");
                btnAdminBack.setVisibility(View.GONE);
                loadFragment(new AdminMoreFragment());
                fabAddRoom.hide();
                return true;
            }
            return false;
        });

        fabAddRoom.setOnClickListener(v -> startActivity(new Intent(this, AdminAddEditRoomActivity.class)));

        showDashboard();
    }

    public void showDashboard() {
        tvAdminTitle.setText("Quản trị hệ thống");
        btnAdminBack.setVisibility(View.GONE);
        fabAddRoom.hide();
        loadFragment(new AdminDashboardFragment());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameAdmin, fragment).commit();
    }

    private void updateHeader(String title) {
        tvAdminTitle.setText(title);
        btnAdminBack.setVisibility(View.VISIBLE);
    }

    public void loadAdminRooms() {
        updateHeader("Quản lý Phòng");
        fabAddRoom.show();
        setupRecycler(new AdminRoomAdapter(roomList, new AdminRoomAdapter.OnAdminRoomActionListener() {
            @Override public void onEdit(Room r) {
                Intent i = new Intent(AdminHomeActivity.this, AdminAddEditRoomActivity.class);
                i.putExtra(Constants.EXTRA_ROOM, r);
                startActivity(i);
            }
            @Override public void onDelete(Room r) { deleteRoom(r); }
        }));
        db.collection(Constants.COLLECTION_ROOMS).get().addOnSuccessListener(snap -> {
            roomList.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Room r = doc.toObject(Room.class);
                r.setId(doc.getId());
                roomList.add(r);
            }
            if (roomAdapter != null) roomAdapter.notifyDataSetChanged();
        });
    }

    public void loadAdminBookings() {
        updateHeader("Quản lý Đặt phòng");
        fabAddRoom.hide();
        setupRecycler(new AdminBookingAdapter(bookingList, new AdminBookingAdapter.OnAdminBookingActionListener() {
            @Override public void onApprove(Booking b) { updateBookingStatus(b, Constants.STATUS_CONFIRMED); }
            @Override public void onReject(Booking b) { updateBookingStatus(b, Constants.STATUS_CANCELLED); }
        }));
        db.collection(Constants.COLLECTION_BOOKINGS).get().addOnSuccessListener(snap -> {
            bookingList.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                bookingList.add(b);
            }
            if (bookingAdapter != null) bookingAdapter.notifyDataSetChanged();
        });
    }

    public void loadAdminChats() {
        updateHeader("Hỗ trợ khách hàng");
        fabAddRoom.hide();
        setupRecycler(new ChatConversationAdapter(conversationList, (uid, name) -> {
            Intent i = new Intent(this, AdminChatDetailActivity.class);
            i.putExtra(Constants.EXTRA_USER_ID, uid);
            i.putExtra(Constants.EXTRA_USER_NAME, name);
            startActivity(i);
        }));
        db.collection(Constants.COLLECTION_CHATS).orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    conversationList.clear();
                    for (QueryDocumentSnapshot doc : snap) conversationList.add(doc.getData());
                    if (chatAdapter != null) chatAdapter.notifyDataSetChanged();
                });
    }

    private void setupRecycler(RecyclerView.Adapter adapter) {
        android.widget.FrameLayout frame = findViewById(R.id.frameAdmin);
        frame.removeAllViews();
        RecyclerView recycler = new RecyclerView(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        frame.addView(recycler);
        if (adapter instanceof AdminRoomAdapter) roomAdapter = (AdminRoomAdapter) adapter;
        if (adapter instanceof AdminBookingAdapter) bookingAdapter = (AdminBookingAdapter) adapter;
        if (adapter instanceof ChatConversationAdapter) chatAdapter = (ChatConversationAdapter) adapter;
    }

    void updateBookingStatus(Booking b, String status) {
        db.collection(Constants.COLLECTION_BOOKINGS).document(b.getId()).update("status", status).addOnSuccessListener(u -> {
            b.setStatus(status);
            if (bookingAdapter != null) bookingAdapter.notifyDataSetChanged();
        });
    }

    void deleteRoom(Room r) {
        db.collection(Constants.COLLECTION_ROOMS).document(r.getId()).delete().addOnSuccessListener(u -> loadAdminRooms());
    }
}