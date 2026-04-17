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
import com.nhom4.hotelbooking.models.Park;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ParkHorizontalAdapter extends RecyclerView.Adapter<ParkHorizontalAdapter.ViewHolder> {

    private List<Park> parkList;

    public ParkHorizontalAdapter(List<Park> parkList) {
        this.parkList = parkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_park_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Park park = parkList.get(position);
        holder.tvName.setText(park.getName());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText("Chỉ từ " + formatter.format(park.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(park.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgPark);

        // Trong onBindViewHolder của ParkHorizontalAdapter.java
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.nhom4.hotelbooking.activities.ParkDetailActivity.class);
            intent.putExtra(com.nhom4.hotelbooking.utils.Constants.EXTRA_PARK, park);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return parkList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPark;
        TextView tvName, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPark = itemView.findViewById(R.id.imgPark);
            tvName = itemView.findViewById(R.id.tvParkName);
            tvPrice = itemView.findViewById(R.id.tvParkPrice);
        }
    }
}