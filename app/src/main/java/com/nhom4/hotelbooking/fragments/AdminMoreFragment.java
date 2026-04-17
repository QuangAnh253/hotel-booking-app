package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.AdminAccountActivity;
import com.nhom4.hotelbooking.activities.AdminHomeActivity;
import com.nhom4.hotelbooking.activities.AdminNewsActivity;
import com.nhom4.hotelbooking.activities.AdminParkActivity;

public class AdminMoreFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_more, container, false);

        setupClicks(view);

        return view;
    }

    private void setupClicks(View v) {
        // 1. Quản lý phòng
        v.findViewById(R.id.btnAdminMoreRooms).setOnClickListener(view -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminRooms();
            }
        });

        // 2. Quản lý tin tức
        v.findViewById(R.id.btnAdminMoreNews).setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), AdminNewsActivity.class));
        });

        // 3. Quản lý tài khoản
        v.findViewById(R.id.btnAdminMoreAccounts).setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), AdminAccountActivity.class));
        });

        // 4. Quản lý Park
        v.findViewById(R.id.btnAdminMoreParks).setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), AdminParkActivity.class));
        });

        // 5. Quản lý Booking
        v.findViewById(R.id.btnAdminMoreBookings).setOnClickListener(view -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminBookings();
            }
        });

        // 6. Truy cập Chat
        v.findViewById(R.id.btnAdminMoreChat).setOnClickListener(view -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminChats();
            }
        });

        // 7. Sửa About
        v.findViewById(R.id.btnAdminMoreAbout).setOnClickListener(view -> {
            Toast.makeText(getContext(), "Tính năng Sửa About đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // 8. Cài đặt
        v.findViewById(R.id.btnAdminMoreSettings).setOnClickListener(view -> {
            Toast.makeText(getContext(), "Tính năng Cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
}