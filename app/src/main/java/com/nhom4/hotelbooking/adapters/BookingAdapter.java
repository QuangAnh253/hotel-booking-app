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

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    List<Booking> bookingList;
    OnCancelClickListener listener;

    public interface OnCancelClickListener {
        void onCancel(Booking booking);
    }

    public BookingAdapter(List<Booking> bookingList, OnCancelClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvBookingRoomName.setText(booking.getRoomName());
        holder.tvBookingDate.setText(booking.getCheckIn() + " → " + booking.getCheckOut());
        holder.tvBookingTotal.setText(String.format("Tổng: %,.0f VNĐ", booking.getTotalPrice()));
        holder.tvBookingStatus.setText("Trạng thái: " + booking.getStatus());

        if (booking.getStatus().equals(Constants.STATUS_PENDING)) {
            holder.tvBookingStatus.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
            holder.btnCancelBooking.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancelBooking.setVisibility(View.GONE);
        }

        holder.btnCancelBooking.setOnClickListener(v -> listener.onCancel(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingRoomName, tvBookingDate, tvBookingTotal, tvBookingStatus;
        Button btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingRoomName = itemView.findViewById(R.id.tvBookingRoomName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingTotal = itemView.findViewById(R.id.tvBookingTotal);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}
