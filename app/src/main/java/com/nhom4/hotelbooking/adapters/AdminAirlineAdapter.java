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
import com.nhom4.hotelbooking.models.Airline;

import java.util.List;

public class AdminAirlineAdapter extends RecyclerView.Adapter<AdminAirlineAdapter.ViewHolder> {

    private List<Airline> airlineList;
    private OnAirlineActionListener listener;

    public interface OnAirlineActionListener {
        void onEdit(Airline airline);
        void onDelete(Airline airline);
    }

    public AdminAirlineAdapter(List<Airline> airlineList, OnAirlineActionListener listener) {
        this.airlineList = airlineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_airline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Airline airline = airlineList.get(position);
        holder.tvName.setText(airline.getName());
        holder.tvUrl.setText(airline.getWebUrl());

        Glide.with(holder.itemView.getContext())
                .load(airline.getLogoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgLogo);

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Chỉnh sửa");
            popup.getMenu().add("Xoá hãng");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Chỉnh sửa")) listener.onEdit(airline);
                else listener.onDelete(airline);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return airlineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvName, tvUrl;
        ImageButton btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.imgAdminAirlineLogo);
            tvName = itemView.findViewById(R.id.tvAdminAirlineName);
            tvUrl = itemView.findViewById(R.id.tvAdminAirlineUrl);
            btnMenu = itemView.findViewById(R.id.btnAdminAirlineMenu);
        }
    }
}