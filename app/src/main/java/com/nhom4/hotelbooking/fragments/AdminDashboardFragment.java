package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.AdminAccountActivity;
import com.nhom4.hotelbooking.activities.AdminAirlineActivity;
import com.nhom4.hotelbooking.activities.AdminHomeActivity;
import com.nhom4.hotelbooking.activities.AdminNewsActivity;
import com.nhom4.hotelbooking.activities.AdminParkActivity;

public class AdminDashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // 1. Quản lý Phòng
        view.findViewById(R.id.cardManageRooms).setOnClickListener(v -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminRooms();
            }
        });

        // 2. Quản lý Hãng hàng không (Mới thêm)
        view.findViewById(R.id.cardManageAirlines).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminAirlineActivity.class));
        });

        // 3. Quản lý Tin tức
        view.findViewById(R.id.cardManageNews).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminNewsActivity.class));
        });

        // 4. Quản lý Khu vui chơi
        view.findViewById(R.id.cardManageParks).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminParkActivity.class));
        });

        // 5. Chat CSKH
        view.findViewById(R.id.cardManageChat).setOnClickListener(v -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminChats();
            }
        });

        // 6. Quản lý Tài khoản
        view.findViewById(R.id.cardManageAccounts).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminAccountActivity.class));
        });

        // 7. Quản lý Booking
        view.findViewById(R.id.cardManageBookings).setOnClickListener(v -> {
            if (getActivity() instanceof AdminHomeActivity) {
                ((AdminHomeActivity) getActivity()).loadAdminBookings();
            }
        });

        return view;
    }
}