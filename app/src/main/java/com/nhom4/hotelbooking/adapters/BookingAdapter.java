package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Booking;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onCancel(Booking booking);
        void onContact(Booking booking);
        void onReview(Booking booking); // Thêm sự kiện đánh giá
        void onItemClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookingList, OnBookingActionListener listener) {
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
        holder.tvBookingTotal.setText(String.format("Tổng tiền: %,.0f VNĐ", booking.getTotalPrice()));
        holder.tvBookingStatus.setText(booking.getStatus().toUpperCase());

        // Xử lý hiển thị nút và màu sắc trạng thái
        if (Constants.STATUS_PENDING.equals(booking.getStatus())) {
            holder.tvBookingStatus.setTextColor(android.graphics.Color.parseColor("#E65100"));
            holder.btnCancelBooking.setVisibility(View.VISIBLE);
            holder.btnReviewBooking.setVisibility(View.GONE);
        } else if (Constants.STATUS_CONFIRMED.equals(booking.getStatus())) {
            holder.tvBookingStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            holder.btnCancelBooking.setVisibility(View.GONE);
            // Hiện nút đánh giá khi đã hoàn thành và chưa đánh giá
            holder.btnReviewBooking.setVisibility(booking.isReviewed() ? View.GONE : View.VISIBLE);
        } else {
            holder.tvBookingStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            holder.btnCancelBooking.setVisibility(View.GONE);
            holder.btnReviewBooking.setVisibility(View.GONE);
        }

        holder.btnCancelBooking.setOnClickListener(v -> listener.onCancel(booking));
        holder.btnContactSupport.setOnClickListener(v -> listener.onContact(booking));
        holder.btnReviewBooking.setOnClickListener(v -> listener.onReview(booking));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingRoomName, tvBookingDate, tvBookingTotal, tvBookingStatus;
        MaterialButton btnCancelBooking, btnContactSupport, btnReviewBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingRoomName = itemView.findViewById(R.id.tvBookingRoomName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingTotal = itemView.findViewById(R.id.tvBookingTotal);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            btnContactSupport = itemView.findViewById(R.id.btnContactSupport);
            btnReviewBooking = itemView.findViewById(R.id.btnReviewBooking);
        }
    }
}
