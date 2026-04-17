package com.nhom4.hotelbooking.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.activities.ParkDetailActivity;
import com.nhom4.hotelbooking.models.Park;
import com.nhom4.hotelbooking.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.ParkViewHolder> {

    private List<Park> parkList;

    public ParkAdapter(List<Park> parkList) {
        this.parkList = parkList;
    }

    @NonNull
    @Override
    public ParkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_park_guest, parent, false);
        return new ParkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkViewHolder holder, int position) {
        Park park = parkList.get(position);
        holder.tvName.setText(park.getName());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText("Chỉ từ " + formatter.format(park.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(park.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgPark);

        // Sự kiện click mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ParkDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PARK, park);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return parkList.size();
    }

    static class ParkViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPark;
        TextView tvName, tvPrice;

        public ParkViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPark = itemView.findViewById(R.id.imgParkGuest);
            tvName = itemView.findViewById(R.id.tvParkGuestName);
            tvPrice = itemView.findViewById(R.id.tvParkGuestPrice);
        }
    }
}