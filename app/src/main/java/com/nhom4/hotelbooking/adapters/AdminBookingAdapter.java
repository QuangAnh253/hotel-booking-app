package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    List<Booking> bookingList;
    OnAdminBookingActionListener listener;

    public interface OnAdminBookingActionListener {
        void onApprove(Booking booking);
        void onReject(Booking booking);
    }

    public AdminBookingAdapter(List<Booking> bookingList, OnAdminBookingActionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvAdminBookingRoomName.setText(booking.getRoomName());
        holder.tvAdminBookingDate.setText(booking.getCheckIn() + " → " + booking.getCheckOut());
        holder.tvAdminBookingTotal.setText(String.format("Tổng: %,.0f VNĐ", booking.getTotalPrice()));
        holder.tvAdminBookingStatus.setText(booking.getStatus().toUpperCase());

        // Mặc định hiện UID trước, sau đó thay bằng thông tin thật
        holder.tvAdminBookingUser.setText("Đang tải...");
        loadUserInfo(booking.getUserId(), holder.tvAdminBookingUser);

        // Hiển thị/Ẩn nút dựa trên trạng thái PENDING
        if (Constants.STATUS_PENDING.equals(booking.getStatus())) {
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
        } else {
            holder.layoutAdminActions.setVisibility(View.GONE);
        }

        holder.btnApproveBooking.setOnClickListener(v -> listener.onApprove(booking));
        holder.btnRejectBooking.setOnClickListener(v -> listener.onReject(booking));
    }

    void loadUserInfo(String userId, TextView tvAdminBookingUser) {
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String phone = snapshot.getString("phone");
                        tvAdminBookingUser.setText("Khách: " + name + " | " + phone);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminBookingRoomName, tvAdminBookingUser, tvAdminBookingDate,
                tvAdminBookingTotal, tvAdminBookingStatus;
        MaterialButton btnApproveBooking, btnRejectBooking;
        View layoutAdminActions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminBookingRoomName = itemView.findViewById(R.id.tvAdminBookingRoomName);
            tvAdminBookingUser = itemView.findViewById(R.id.tvAdminBookingUser);
            tvAdminBookingDate = itemView.findViewById(R.id.tvAdminBookingDate);
            tvAdminBookingTotal = itemView.findViewById(R.id.tvAdminBookingTotal);
            tvAdminBookingStatus = itemView.findViewById(R.id.tvAdminBookingStatus);
            btnApproveBooking = itemView.findViewById(R.id.btnApproveBooking);
            btnRejectBooking = itemView.findViewById(R.id.btnRejectBooking);
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
        }
    }
}
