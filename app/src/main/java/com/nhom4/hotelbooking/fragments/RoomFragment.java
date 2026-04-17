package com.nhom4.hotelbooking.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.RoomDetailActivity;
import com.nhom4.hotelbooking.adapters.RoomAdapter;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RoomFragment extends Fragment {

    private RecyclerView recyclerRooms;
    private EditText edtSearch;
    private Spinner spinnerType, spinnerSort;
    private TextView tvCheckIn, tvCheckOut;
    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private List<Room> roomListFull = new ArrayList<>();
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Calendar checkInDate, checkOutDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        db = FirebaseFirestore.getInstance();
        initViews(view);
        setupSpinners();
        setupDatePickers();

        recyclerRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomAdapter(roomList, room -> {
            Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ROOM, room);
            startActivity(intent);
        });
        recyclerRooms.setAdapter(adapter);

        view.findViewById(R.id.btnApplyFilter).setOnClickListener(v -> applyFilters());

        loadAllRooms();

        return view;
    }

    private void initViews(View v) {
        recyclerRooms = v.findViewById(R.id.recyclerRooms);
        edtSearch = v.findViewById(R.id.edtSearch);
        spinnerType = v.findViewById(R.id.spinnerType);
        spinnerSort = v.findViewById(R.id.spinnerSort);
        tvCheckIn = v.findViewById(R.id.tvCheckIn);
        tvCheckOut = v.findViewById(R.id.tvCheckOut);
    }

    private void setupSpinners() {
        String[] types = {"Tất cả loại phòng", "Standard", "Deluxe", "Suite"};
        spinnerType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types));

        String[] sortOptions = {"Sắp xếp: Mặc định", "Giá tăng dần", "Giá giảm dần"};
        spinnerSort.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions));
    }

    private void setupDatePickers() {
        tvCheckIn.setOnClickListener(v -> showDatePicker(true));
        tvCheckOut.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            if (isCheckIn) {
                checkInDate = selected;
                tvCheckIn.setText(sdf.format(selected.getTime()));
            } else {
                checkOutDate = selected;
                tvCheckOut.setText(sdf.format(selected.getTime()));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadAllRooms() {
        db.collection(Constants.COLLECTION_ROOMS).get().addOnSuccessListener(snap -> {
            roomListFull.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Room r = doc.toObject(Room.class);
                r.setId(doc.getId());
                roomListFull.add(r);
            }
            applyFilters(); // Ban đầu hiện tất cả
        });
    }

    private void applyFilters() {
        String keyword = edtSearch.getText().toString().toLowerCase().trim();
        String type = spinnerType.getSelectedItem().toString();
        int sortPos = spinnerSort.getSelectedItemPosition();

        if (checkInDate != null && checkOutDate != null) {
            filterByAvailability(keyword, type, sortPos);
        } else {
            performFinalFilter(new HashSet<>(), keyword, type, sortPos);
        }
    }

    private void filterByAvailability(String keyword, String type, int sortPos) {
        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereNotEqualTo("status", Constants.STATUS_CANCELLED)
                .get()
                .addOnSuccessListener(snap -> {
                    Set<String> busyRoomIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        try {
                            Booking b = doc.toObject(Booking.class);
                            Date bStart = sdf.parse(b.getCheckIn());
                            Date bEnd = sdf.parse(b.getCheckOut());
                            Date qStart = checkInDate.getTime();
                            Date qEnd = checkOutDate.getTime();

                            // Logic: overlaps if (StartA <= EndB) and (EndA >= StartB)
                            if (qStart.before(bEnd) && qEnd.after(bStart)) {
                                busyRoomIds.add(b.getRoomId());
                            }
                        } catch (Exception ignored) {}
                    }
                    performFinalFilter(busyRoomIds, keyword, type, sortPos);
                });
    }

    private void performFinalFilter(Set<String> busyIds, String keyword, String type, int sortPos) {
        roomList.clear();
        for (Room r : roomListFull) {
            if (busyIds.contains(r.getId())) continue;
            if (!keyword.isEmpty() && !r.getName().toLowerCase().contains(keyword)) continue;
            if (!type.equals("Tất cả loại phòng") && !r.getType().equals(type)) continue;
            roomList.add(r);
        }

        // Sắp xếp
        if (sortPos == 1) {
            Collections.sort(roomList, (r1, r2) -> Double.compare(r1.getPrice(), r2.getPrice()));
        } else if (sortPos == 2) {
            Collections.sort(roomList, (r1, r2) -> Double.compare(r2.getPrice(), r1.getPrice()));
        }

        adapter.notifyDataSetChanged();
        if (roomList.isEmpty()) Toast.makeText(getContext(), "Không tìm thấy phòng phù hợp", Toast.LENGTH_SHORT).show();
    }
}