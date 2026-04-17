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
import com.nhom4.hotelbooking.models.Park;

import java.util.List;

public class AdminParkAdapter extends RecyclerView.Adapter<AdminParkAdapter.AdminParkViewHolder> {

    private List<Park> parkList;
    private OnAdminParkActionListener listener;

    public interface OnAdminParkActionListener {
        void onEdit(Park park);
        void onDelete(Park park);
    }

    public AdminParkAdapter(List<Park> parkList, OnAdminParkActionListener listener) {
        this.parkList = parkList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminParkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_park, parent, false);
        return new AdminParkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminParkViewHolder holder, int position) {
        Park park = parkList.get(position);
        holder.tvAdminParkName.setText(park.getName());
        holder.tvAdminParkPrice.setText(String.format("%,.0f VNĐ", park.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(park.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgAdminPark);

        holder.btnAdminParkMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnAdminParkMenu);
            popup.getMenu().add(0, 1, 0, "Sửa");
            popup.getMenu().add(0, 2, 0, "Xoá");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) listener.onEdit(park);
                else if (item.getItemId() == 2) listener.onDelete(park);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return parkList.size();
    }

    static class AdminParkViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAdminPark;
        TextView tvAdminParkName, tvAdminParkPrice;
        ImageButton btnAdminParkMenu;

        public AdminParkViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAdminPark = itemView.findViewById(R.id.imgAdminPark);
            tvAdminParkName = itemView.findViewById(R.id.tvAdminParkName);
            tvAdminParkPrice = itemView.findViewById(R.id.tvAdminParkPrice);
            btnAdminParkMenu = itemView.findViewById(R.id.btnAdminParkMenu);
        }
    }
}