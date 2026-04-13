package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.tvAdminBookingRoom.setText(booking.getRoomName());
        holder.tvAdminBookingUser.setText("User: " + booking.getUserId());
        holder.tvAdminBookingDate.setText(booking.getCheckIn() + " → " + booking.getCheckOut());
        holder.tvAdminBookingTotal.setText(String.format("Tổng: %,.0f VNĐ", booking.getTotalPrice()));
        holder.tvAdminBookingStatus.setText("Trạng thái: " + booking.getStatus());

        // Chỉ hiện nút Duyệt/Huỷ khi status là "pending"
        if (booking.getStatus().equals(Constants.STATUS_PENDING)) {
            holder.tvAdminBookingStatus.setTextColor(0xFFFF8C00);
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(booking));
        holder.btnReject.setOnClickListener(v -> listener.onReject(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminBookingRoom, tvAdminBookingUser, tvAdminBookingDate,
                tvAdminBookingTotal, tvAdminBookingStatus;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminBookingRoom = itemView.findViewById(R.id.tvAdminBookingRoom);
            tvAdminBookingUser = itemView.findViewById(R.id.tvAdminBookingUser);
            tvAdminBookingDate = itemView.findViewById(R.id.tvAdminBookingDate);
            tvAdminBookingTotal = itemView.findViewById(R.id.tvAdminBookingTotal);
            tvAdminBookingStatus = itemView.findViewById(R.id.tvAdminBookingStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}