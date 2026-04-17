package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.MainActivity;
import com.nhom4.hotelbooking.activities.NewsActivity;

public class MoreFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        setupClicks(view);

        return view;
    }

    private void setupClicks(View v) {
        // 1. Lưu trú
        v.findViewById(R.id.btnMoreRoom).setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchTab(R.id.nav_more_redirect);
            }
        });

        // 2. PHÒNG YÊU THÍCH (ĐÃ SỬA)
        v.findViewById(R.id.btnMoreFavorite).setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new FavoriteRoomsFragment());
            }
        });

        // 3. Tin tức
        v.findViewById(R.id.btnMoreNews).setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), NewsActivity.class));
        });

        // 4. Về Hotel booking app
        v.findViewById(R.id.btnMoreAbout).setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new AboutUsWebViewFragment());
            }
        });

        // 5. Cài đặt
        v.findViewById(R.id.btnMoreSettings).setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new SettingsFragment());
            }
        });

        // 6. Đánh giá ngay
        v.findViewById(R.id.btnMoreRate).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getContext().getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
            }
        });
    }
}