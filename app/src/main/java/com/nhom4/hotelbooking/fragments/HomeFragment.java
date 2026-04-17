package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.AirlineGuestActivity;
import com.nhom4.hotelbooking.activities.MainActivity;
import com.nhom4.hotelbooking.activities.NewsActivity;
import com.nhom4.hotelbooking.activities.ParkActivity;
import com.nhom4.hotelbooking.activities.RoomDetailActivity;
import com.nhom4.hotelbooking.adapters.NewsAdapter;
import com.nhom4.hotelbooking.adapters.ParkHorizontalAdapter;
import com.nhom4.hotelbooking.adapters.RoomSuggestionAdapter;
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvHomeUsername;
    private ImageView ivNotification, ivHomeHeaderBg;
    private RecyclerView recyclerParksHome, recyclerSuggestions, recyclerNewsHome;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private List<Room> suggestionList = new ArrayList<>();
    private RoomSuggestionAdapter suggestionAdapter;
    private List<Park> parkList = new ArrayList<>();
    private ParkHorizontalAdapter parkAdapter;
    private List<News> newsList = new ArrayList<>();
    private NewsAdapter newsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvHomeUsername = view.findViewById(R.id.tvHomeUsername);
        ivNotification = view.findViewById(R.id.ivNotification);
        ivHomeHeaderBg = view.findViewById(R.id.ivHomeHeaderBg);
        recyclerParksHome = view.findViewById(R.id.recyclerParksHome);
        recyclerSuggestions = view.findViewById(R.id.recyclerSuggestions);
        recyclerNewsHome = view.findViewById(R.id.recyclerNewsHome);

        // GIẢI PHÁP CHỐNG CRASH TẬN GỐC CHO SAMSUNG S7 EDGE:
        // Nạp ảnh header lớn qua Glide với độ phân giải thấp (Downsampling)
        if (ivHomeHeaderBg != null) {
            Glide.with(this)
                    .load(R.drawable.bg_home_header)
                    .override(1080, 500) // Thu nhỏ ảnh ngay trong RAM
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivHomeHeaderBg);
        }

        setupUserInfo();
        setupMenus(view);
        setupParksHome();      
        setupSuggestions();    
        setupNewsHome();       
        
        if (parkList.isEmpty()) loadParksData();
        if (suggestionList.isEmpty()) loadRandomRooms();
        if (newsList.isEmpty()) loadLatestNews();

        ivNotification.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchTab(R.id.nav_history);
            }
        });

        return view;
    }

    private void setupUserInfo() {
        if (mAuth.getCurrentUser() != null) {
            db.collection(Constants.COLLECTION_USERS).document(mAuth.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && documentSnapshot.exists()) {
                            tvHomeUsername.setText(documentSnapshot.getString("name"));
                        }
                    });
        }
    }

    private void setupMenus(View v) {
        v.findViewById(R.id.menuHotel).setOnClickListener(view -> 
                ((MainActivity) getActivity()).switchTab(R.id.nav_more_redirect));
        v.findViewById(R.id.menuNews).setOnClickListener(view -> 
                startActivity(new Intent(getActivity(), NewsActivity.class)));
        v.findViewById(R.id.menuPark).setOnClickListener(view -> 
                startActivity(new Intent(getActivity(), ParkActivity.class)));
        v.findViewById(R.id.menuFlight).setOnClickListener(view -> 
                startActivity(new Intent(getActivity(), AirlineGuestActivity.class)));
    }

    private void setupParksHome() {
        recyclerParksHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerParksHome.setHasFixedSize(true);
        recyclerParksHome.setNestedScrollingEnabled(false);
        parkAdapter = new ParkHorizontalAdapter(parkList);
        recyclerParksHome.setAdapter(parkAdapter);
    }

    private void setupSuggestions() {
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSuggestions.setHasFixedSize(true);
        recyclerSuggestions.setNestedScrollingEnabled(false);
        suggestionAdapter = new RoomSuggestionAdapter(suggestionList, room -> {
            Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivity(intent);
        });
        recyclerSuggestions.setAdapter(suggestionAdapter);
    }

    private void setupNewsHome() {
        recyclerNewsHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerNewsHome.setHasFixedSize(true);
        recyclerNewsHome.setNestedScrollingEnabled(false);
        newsAdapter = new NewsAdapter(newsList, true);
        recyclerNewsHome.setAdapter(newsAdapter);
    }

    private void loadParksData() {
        db.collection(Constants.COLLECTION_PARKS).limit(6).get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded()) return;
                    parkList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        parkList.add(doc.toObject(Park.class));
                    }
                    parkAdapter.notifyDataSetChanged();
                });
    }

    private void loadLatestNews() {
        db.collection(Constants.COLLECTION_NEWS)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded()) return;
                    newsList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        newsList.add(doc.toObject(News.class));
                    }
                    newsAdapter.notifyDataSetChanged();
                });
    }

    private void loadRandomRooms() {
        db.collection(Constants.COLLECTION_ROOMS).get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded()) return;
                    suggestionList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Room room = doc.toObject(Room.class);
                        room.setId(doc.getId());
                        suggestionList.add(room);
                    }
                    Collections.shuffle(suggestionList);
                    suggestionAdapter.notifyDataSetChanged();
                });
    }
}