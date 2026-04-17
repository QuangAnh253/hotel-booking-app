package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.LobbyActivity;
import com.nhom4.hotelbooking.activities.MainActivity;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.btnBackSettings).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new MoreFragment());
            }
        });

        setupSettings(view);

        return view;
    }

    private void setupSettings(View v) {
        // 1. Đổi mật khẩu
        v.findViewById(R.id.btnSettingChangePass).setOnClickListener(view -> {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (email != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Link đổi mật khẩu đã gửi tới " + email, Toast.LENGTH_LONG).show());
            }
        });

        // 2. Đăng xuất
        v.findViewById(R.id.btnSettingLogout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        // 3. Điều khoản chung (Mở WebView)
        v.findViewById(R.id.btnSettingTerms).setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new TermsWebViewFragment());
            }
        });

        // Các tính năng khác (Placeholder)
        v.findViewById(R.id.btnSettingLanguage).setOnClickListener(view -> Toast.makeText(getContext(), "Tính năng Đổi ngôn ngữ đang phát triển", Toast.LENGTH_SHORT).show());
        v.findViewById(R.id.btnSettingNotify).setOnClickListener(view -> Toast.makeText(getContext(), "Tính năng Thông báo đang phát triển", Toast.LENGTH_SHORT).show());
    }
}