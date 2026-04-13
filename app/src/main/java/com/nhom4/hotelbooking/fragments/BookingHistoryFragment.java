package com.nhom4.hotelbooking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.BookingAdapter;
import com.nhom4.hotelbooking.database.DatabaseHelper;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryFragment extends Fragment {

    RecyclerView recyclerBookings;
    BookingAdapter adapter;
    List<Booking> bookingList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_history, container, false);

        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(getContext());

        bookingList = new ArrayList<>();
        recyclerBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(bookingList, booking -> cancelBooking(booking));
        recyclerBookings.setAdapter(adapter);

        loadBookings();

        return view;
    }

    void loadBookings() {
        String uid = mAuth.getCurrentUser().getUid();
        List<Booking> localList = dbHelper.getAllBookings(uid);
        if (!localList.isEmpty()) {
            bookingList.clear();
            bookingList.addAll(localList);
            adapter.notifyDataSetChanged();
        } else {
            syncFromFirestore(uid);
        }
    }

    void syncFromFirestore(String uid) {
        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    bookingList.clear();
                    for (var doc : querySnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        bookingList.add(booking);
                        dbHelper.insertBooking(booking);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    void cancelBooking(Booking booking) {
        db.collection(Constants.COLLECTION_BOOKINGS).document(booking.getId())
                .update("status", Constants.STATUS_CANCELLED)
                .addOnSuccessListener(unused -> {
                    dbHelper.updateBookingStatus(booking.getId(), Constants.STATUS_CANCELLED);
                    booking.setStatus(Constants.STATUS_CANCELLED);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã huỷ đặt phòng", Toast.LENGTH_SHORT).show();
                });
    }
}
