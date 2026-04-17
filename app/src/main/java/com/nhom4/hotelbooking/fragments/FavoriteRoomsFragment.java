package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.MainActivity;
import com.nhom4.hotelbooking.activities.RoomDetailActivity;
import com.nhom4.hotelbooking.adapters.RoomAdapter;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRoomsFragment extends Fragment {

    private RecyclerView recyclerFavorites;
    private TextView tvNoFavorites;
    private RoomAdapter adapter;
    private List<Room> favoriteList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_rooms, container, false);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        recyclerFavorites = view.findViewById(R.id.recyclerFavorites);
        tvNoFavorites = view.findViewById(R.id.tvNoFavorites);

        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomAdapter(favoriteList, room -> {
            Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivity(intent);
        });
        recyclerFavorites.setAdapter(adapter);

        view.findViewById(R.id.btnBackFavorite).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new MoreFragment());
            }
        });

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        if (userId == null) return;

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> roomIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        roomIds.add(doc.getString("roomId"));
                    }

                    if (roomIds.isEmpty()) {
                        favoriteList.clear();
                        adapter.notifyDataSetChanged();
                        tvNoFavorites.setVisibility(View.VISIBLE);
                        recyclerFavorites.setVisibility(View.GONE);
                    } else {
                        tvNoFavorites.setVisibility(View.GONE);
                        recyclerFavorites.setVisibility(View.VISIBLE);
                        fetchRooms(roomIds);
                    }
                });
    }

    private void fetchRooms(List<String> ids) {
        // Firestore 'in' query supports max 10 elements. For simplicity in school project:
        db.collection(Constants.COLLECTION_ROOMS)
                .get()
                .addOnSuccessListener(snap -> {
                    favoriteList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        if (ids.contains(doc.getId())) {
                            Room r = doc.toObject(Room.class);
                            r.setId(doc.getId());
                            favoriteList.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}