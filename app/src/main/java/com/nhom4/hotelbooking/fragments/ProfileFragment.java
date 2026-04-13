package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.LoginActivity;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    EditText edtProfileName, edtProfilePhone, edtProfileEmail, edtNewPassword;
    Button btnUpdateProfile, btnChangePassword, btnLogout;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        edtProfileName  = view.findViewById(R.id.edtProfileName);
        edtProfilePhone = view.findViewById(R.id.edtProfilePhone);
        edtProfileEmail = view.findViewById(R.id.edtProfileEmail);
        edtNewPassword  = view.findViewById(R.id.edtNewPassword);

        btnUpdateProfile  = view.findViewById(R.id.btnUpdateProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout         = view.findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        loadProfile();

        btnUpdateProfile.setOnClickListener(v  -> updateProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnLogout.setOnClickListener(v         -> logout());

        return view;
    }

    void loadProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "loadProfile: currentUser is null");
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "Loading profile for uid: " + uid);

        db.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return; // fragment đã bị detach

                    if (snapshot == null || !snapshot.exists()) {
                        Log.e(TAG, "Document không tồn tại trong Firestore! uid=" + uid);
                        Toast.makeText(getContext(),
                                "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Log toàn bộ data để kiểm tra field name thực tế
                    Log.d(TAG, "Firestore data: " + snapshot.getData());

                    String name  = snapshot.getString("name");
                    String phone = snapshot.getString("phone");
                    String email = snapshot.getString("email");

                    Log.d(TAG, "name=" + name + " | phone=" + phone + " | email=" + email);

                    edtProfileName.setText(name  != null ? name  : "");
                    edtProfilePhone.setText(phone != null ? phone : "");

                    // Email: ưu tiên Firestore, fallback sang Firebase Auth
                    if (email != null && !email.isEmpty()) {
                        edtProfileEmail.setText(email);
                    } else if (currentUser.getEmail() != null) {
                        edtProfileEmail.setText(currentUser.getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi đọc Firestore: " + e.getMessage());
                    if (isAdded()) {
                        Toast.makeText(getContext(),
                                "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void updateProfile() {
        String name  = edtProfileName.getText().toString().trim();
        String phone = edtProfilePhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() != 10) {
            Toast.makeText(getContext(), "Số điện thoại phải có 10 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name",  name);
        updates.put("phone", phone);

        db.collection(Constants.COLLECTION_USERS).document(uid).update(updates)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    void changePassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        user.updatePassword(newPassword)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    edtNewPassword.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Lỗi: cần đăng nhập lại trước khi đổi mật khẩu", Toast.LENGTH_SHORT).show());
    }

    void logout() {
        mAuth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        if (getActivity() != null) getActivity().finish();
    }
}