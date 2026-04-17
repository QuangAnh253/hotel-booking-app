package com.nhom4.hotelbooking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.AdminAccountAdapter;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountActivity extends AppCompatActivity {

    private TabLayout tabLayoutAccounts;
    private RecyclerView recyclerAdminAccounts;
    private AdminAccountAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account);

        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupRecyclerView();
        setupTabs();
        loadAccounts();
    }

    private void initViews() {
        tabLayoutAccounts = findViewById(R.id.tabLayoutAccounts);
        recyclerAdminAccounts = findViewById(R.id.recyclerAdminAccounts);
        findViewById(R.id.btnBackAdminAccount).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerAdminAccounts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAccountAdapter(filteredList, new AdminAccountAdapter.OnAccountActionListener() {
            @Override
            public void onEdit(User user) {
                Intent intent = new Intent(AdminAccountActivity.this, AdminEditAccountActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }

            @Override
            public void onDelete(User user) {
                new AlertDialog.Builder(AdminAccountActivity.this)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn chắc chắn muốn xoá hồ sơ này?")
                        .setPositiveButton("Xoá", (d, w) -> deleteUser(user))
                        .setNegativeButton("Huỷ", null).show();
            }
        });
        recyclerAdminAccounts.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayoutAccounts.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByRole(tab.getPosition() == 0 ? Constants.ROLE_ADMIN : Constants.ROLE_USER);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAccounts() {
        db.collection(Constants.COLLECTION_USERS).get().addOnSuccessListener(snap -> {
            userList.clear();
            for (QueryDocumentSnapshot doc : snap) {
                User user = doc.toObject(User.class);
                user.setUid(doc.getId());
                userList.add(user);
            }
            int currentTab = tabLayoutAccounts.getSelectedTabPosition();
            filterByRole(currentTab == 0 ? Constants.ROLE_ADMIN : Constants.ROLE_USER);
        });
    }

    private void filterByRole(String role) {
        filteredList.clear();
        for (User u : userList) {
            if (role.equals(u.getRole())) filteredList.add(u);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteUser(User user) {
        db.collection(Constants.COLLECTION_USERS).document(user.getUid()).delete().addOnSuccessListener(u -> {
            Toast.makeText(this, "Đã xoá tài khoản", Toast.LENGTH_SHORT).show();
            loadAccounts();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts();
    }
}