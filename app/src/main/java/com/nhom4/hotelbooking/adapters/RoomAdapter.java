package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    List<Room> roomList;
    OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onClick(Room room);
    }

    public RoomAdapter(List<Room> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomType.setText(room.getType());
        holder.tvRoomPrice.setText(String.format("%,.0f VNĐ/đêm", room.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(room.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgRoom);

        holder.itemView.setOnClickListener(v -> listener.onClick(room));
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoom;
        TextView tvRoomName, tvRoomType, tvRoomPrice;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
        }
    }
}
