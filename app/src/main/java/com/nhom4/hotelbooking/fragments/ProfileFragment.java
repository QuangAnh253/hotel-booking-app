package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.AdminEditAccountActivity;
import com.nhom4.hotelbooking.activities.LobbyActivity;
import com.nhom4.hotelbooking.models.User;
import com.nhom4.hotelbooking.utils.Constants;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView tvProfileName, tvProfileEmailSummary;
    private TextView btnShowEditInfo, btnShowChangePass, btnLogout;
    private ImageButton btnEditProfileHeader;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadProfile();

        // Nút sửa ở Header hoặc dòng Menu đều mở trang Edit
        btnEditProfileHeader.setOnClickListener(v -> openEditScreen());
        btnShowEditInfo.setOnClickListener(v -> openEditScreen());
        
        btnShowChangePass.setOnClickListener(v -> {
            // Gửi email reset mật khẩu từ Google
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                mAuth.sendPasswordResetEmail(user.getEmail())
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), 
                                "Link đổi mật khẩu đã được gửi tới email của bạn", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), 
                                "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void initViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmailSummary = view.findViewById(R.id.tvProfileEmailSummary);
        btnShowEditInfo = view.findViewById(R.id.btnShowEditInfo);
        btnShowChangePass = view.findViewById(R.id.btnShowChangePass);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfileHeader = view.findViewById(R.id.btnEditProfileHeader);
    }

    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection(Constants.COLLECTION_USERS).document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || !snapshot.exists()) return;

                    currentUserModel = snapshot.toObject(User.class);
                    if (currentUserModel != null) {
                        currentUserModel.setUid(snapshot.getId());
                        tvProfileName.setText(currentUserModel.getName());
                        tvProfileEmailSummary.setText(currentUserModel.getEmail());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi tải hồ sơ: " + e.getMessage()));
    }

    private void openEditScreen() {
        if (currentUserModel != null) {
            Intent intent = new Intent(getActivity(), AdminEditAccountActivity.class);
            intent.putExtra("user", currentUserModel);
            startActivity(intent);
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LobbyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile(); // Load lại thông tin mới nhất
    }
}