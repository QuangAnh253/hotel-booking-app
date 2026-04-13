package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.RoomDetailActivity;
import com.nhom4.hotelbooking.adapters.RoomAdapter;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recyclerRooms;
    EditText edtSearch;
    Spinner spinnerFilter;

    FirebaseFirestore db;
    RoomAdapter adapter;

    List<Room> roomList;
    List<Room> roomListFull;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        edtSearch = view.findViewById(R.id.edtSearch);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);

        db = FirebaseFirestore.getInstance();
        roomList = new ArrayList<>();
        roomListFull = new ArrayList<>();

        recyclerRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomAdapter(roomList, room -> {
            Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivity(intent);
        });
        recyclerRooms.setAdapter(adapter);

        setupSpinner();
        loadRooms();
        setupSearch();

        return view;
    }

    void loadRooms() {
        db.collection(Constants.COLLECTION_ROOMS)
                .whereEqualTo("status", Constants.STATUS_AVAILABLE)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    roomList.clear();
                    roomListFull.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Room room = doc.toObject(Room.class);
                        room.setId(doc.getId());
                        roomList.add(room);
                        roomListFull.add(room);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải danh sách phòng", Toast.LENGTH_SHORT).show();
                });
    }

    void setupSpinner() {
        String[] types = {"Tất cả", "Standard", "Deluxe", "Suite"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = types[position];
                filterByType(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    void filterByType(String type) {
        roomList.clear();
        if (type.equals("Tất cả")) {
            roomList.addAll(roomListFull);
        } else {
            for (Room room : roomListFull) {
                if (room.getType().equals(type)) {
                    roomList.add(room);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase().trim();
                roomList.clear();
                for (Room room : roomListFull) {
                    if (room.getName().toLowerCase().contains(keyword)) {
                        roomList.add(room);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_price) {
            roomList.sort((r1, r2) -> Double.compare(r1.getPrice(), r2.getPrice()));
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
