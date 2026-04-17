package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Airline;

import java.util.List;

public class AirlineGuestAdapter extends RecyclerView.Adapter<AirlineGuestAdapter.ViewHolder> {

    private List<Airline> airlineList;
    private OnAirlineClickListener listener;

    public interface OnAirlineClickListener {
        void onClick(Airline airline);
    }

    public AirlineGuestAdapter(List<Airline> airlineList, OnAirlineClickListener listener) {
        this.airlineList = airlineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_airline_guest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Airline airline = airlineList.get(position);
        holder.tvName.setText(airline.getName());

        Glide.with(holder.itemView.getContext())
                .load(airline.getLogoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgLogo);

        // Bắt sự kiện cho cả thẻ và nút Truy cập
        View.OnClickListener clickListener = v -> listener.onClick(airline);
        holder.itemView.setOnClickListener(clickListener);
        holder.btnAccess.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return airlineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvName;
        MaterialButton btnAccess;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.imgAirlineLogo);
            tvName = itemView.findViewById(R.id.tvAirlineName);
            btnAccess = itemView.findViewById(R.id.btnAccessAirline);
        }
    }
}