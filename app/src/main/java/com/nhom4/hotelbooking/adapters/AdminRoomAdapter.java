package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;

import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.AdminRoomViewHolder> {

    List<Room> roomList;
    OnAdminRoomActionListener listener;

    public interface OnAdminRoomActionListener {
        void onEdit(Room room);
        void onDelete(Room room);
    }

    public AdminRoomAdapter(List<Room> roomList, OnAdminRoomActionListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_room, parent, false);
        return new AdminRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminRoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.tvAdminRoomName.setText(room.getName());
        holder.tvAdminRoomPrice.setText(String.format("%,.0f VNĐ/đêm", room.getPrice()));
        holder.tvAdminRoomStatus.setText("Trạng thái: " + room.getStatus());

        Glide.with(holder.itemView.getContext())
                .load(room.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgAdminRoom);

        holder.btnAdminRoomMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnAdminRoomMenu);
            popup.getMenu().add(0, 1, 0, "Sửa");
            popup.getMenu().add(0, 2, 0, "Xoá");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    listener.onEdit(room);
                } else if (item.getItemId() == 2) {
                    listener.onDelete(room);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class AdminRoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAdminRoom;
        TextView tvAdminRoomName, tvAdminRoomPrice, tvAdminRoomStatus;
        ImageButton btnAdminRoomMenu;

        public AdminRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAdminRoom = itemView.findViewById(R.id.imgAdminRoom);
            tvAdminRoomName = itemView.findViewById(R.id.tvAdminRoomName);
            tvAdminRoomPrice = itemView.findViewById(R.id.tvAdminRoomPrice);
            tvAdminRoomStatus = itemView.findViewById(R.id.tvAdminRoomStatus);
            btnAdminRoomMenu = itemView.findViewById(R.id.btnAdminRoomMenu);
        }
    }
}
