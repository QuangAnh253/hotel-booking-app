package com.nhom4.hotelbooking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.ChatActivity;
import com.nhom4.hotelbooking.activities.RoomDetailActivity;
import com.nhom4.hotelbooking.adapters.BookingAdapter;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.models.Message;
import com.nhom4.hotelbooking.models.Review;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingHistoryFragment extends Fragment {

    private RecyclerView recyclerBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_history, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        recyclerBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookingAdapter(bookingList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onCancel(Booking booking) {
                cancelBooking(booking);
            }

            @Override
            public void onContact(Booking booking) {
                sendAutomaticMessage(booking);
            }

            @Override
            public void onReview(Booking booking) {
                showReviewDialog(booking);
            }

            @Override
            public void onItemClick(Booking booking) {
                viewRoomDetail(booking.getRoomId());
            }
        });
        recyclerBookings.setAdapter(adapter);

        loadBookings();
        return view;
    }

    private void showReviewDialog(Booking booking) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);
        TextView tvRoomName = dialogView.findViewById(R.id.tvReviewRoomName);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBarInput);
        EditText edtComment = dialogView.findViewById(R.id.edtReviewComment);

        tvRoomName.setText(booking.getRoomName());

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = edtComment.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitReview(booking, rating, comment);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void submitReview(Booking booking, float rating, String comment) {
        String uid = mAuth.getUid();
        db.collection(Constants.COLLECTION_USERS).document(uid).get().addOnSuccessListener(userSnap -> {
            String userName = userSnap.getString("name");
            
            Review review = new Review(uid, userName, booking.getRoomId(), rating, comment, System.currentTimeMillis());
            
            db.collection(Constants.COLLECTION_REVIEWS).add(review).addOnSuccessListener(doc -> {
                // Cập nhật trạng thái đơn hàng đã được đánh giá
                db.collection(Constants.COLLECTION_BOOKINGS).document(booking.getId()).update("isReviewed", true)
                        .addOnSuccessListener(u -> {
                            Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                            loadBookings(); // Tải lại để ẩn nút đánh giá
                        });
            });
        });
    }

    private void loadBookings() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection(Constants.COLLECTION_BOOKINGS)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Booking b = doc.toObject(Booking.class);
                        b.setId(doc.getId());
                        bookingList.add(b);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void cancelBooking(Booking booking) {
        db.collection(Constants.COLLECTION_BOOKINGS).document(booking.getId())
                .update("status", Constants.STATUS_CANCELLED)
                .addOnSuccessListener(u -> {
                    Toast.makeText(getContext(), "Đã huỷ đơn thành công", Toast.LENGTH_SHORT).show();
                    loadBookings();
                });
    }

    private void sendAutomaticMessage(Booking booking) {
        String uid = mAuth.getUid();
        String messageText = "Tôi đang muốn hỏi thông tin về " + booking.getRoomName() + 
                           ", đặt từ " + booking.getCheckIn() + " - " + booking.getCheckOut();
        
        long timestamp = System.currentTimeMillis();
        Message msg = new Message(uid, Constants.ROLE_USER, "Khách hàng", messageText, timestamp);

        db.collection(Constants.COLLECTION_CHATS).document(uid).collection("messages").add(msg)
                .addOnSuccessListener(doc -> {
                    Map<String, Object> conv = new HashMap<>();
                    conv.put("lastMessage", messageText);
                    conv.put("lastMessageTime", timestamp);
                    conv.put("unreadByAdmin", true);
                    db.collection(Constants.COLLECTION_CHATS).document(uid).update(conv);

                    startActivity(new Intent(getActivity(), ChatActivity.class));
                });
    }

    private void viewRoomDetail(String roomId) {
        db.collection(Constants.COLLECTION_ROOMS).document(roomId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Room room = doc.toObject(Room.class);
                        room.setId(doc.getId());
                        Intent i = new Intent(getActivity(), RoomDetailActivity.class);
                        i.putExtra(Constants.EXTRA_ROOM, room);
                        startActivity(i);
                    }
                });
    }
}