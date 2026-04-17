package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

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
                .placeholder(R.mipmap.logo_hotel)
                .into(holder.imgRoom);

        checkFavoriteStatus(room.getId(), holder.btnFavorite);
        loadRoomRating(room.getId(), holder.tvRoomRating);

        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(room, holder.btnFavorite));
        holder.itemView.setOnClickListener(v -> listener.onClick(room));
    }

    private void loadRoomRating(String roomId, TextView tvRating) {
        db.collection(Constants.COLLECTION_REVIEWS)
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        tvRating.setVisibility(View.GONE);
                    } else {
                        double total = 0;
                        for (QueryDocumentSnapshot doc : snap) {
                            total += doc.getDouble("rating");
                        }
                        double avg = total / snap.size();
                        tvRating.setVisibility(View.VISIBLE);
                        tvRating.setText(String.format("%.1f ★ (%d)", avg, snap.size()));
                    }
                });
    }

    private void checkFavoriteStatus(String roomId, ImageButton btn) {
        if (userId == null) return;
        db.collection("favorites").whereEqualTo("userId", userId).whereEqualTo("roomId", roomId).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        btn.setImageResource(R.drawable.ic_favorite_filled);
                        btn.setTag("filled");
                    } else {
                        btn.setImageResource(R.drawable.ic_favorite_border);
                        btn.setTag("border");
                    }
                });
    }

    private void toggleFavorite(Room room, ImageButton btn) {
        if (userId == null) return;
        if ("filled".equals(btn.getTag())) {
            db.collection("favorites").whereEqualTo("userId", userId).whereEqualTo("roomId", room.getId()).get()
                    .addOnSuccessListener(snap -> {
                        for (var doc : snap) doc.getReference().delete();
                        btn.setImageResource(R.drawable.ic_favorite_border);
                        btn.setTag("border");
                    });
        } else {
            Map<String, Object> fav = new HashMap<>();
            fav.put("userId", userId);
            fav.put("roomId", room.getId());
            db.collection("favorites").add(fav).addOnSuccessListener(doc -> {
                btn.setImageResource(R.drawable.ic_favorite_filled);
                btn.setTag("filled");
            });
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoom;
        TextView tvRoomName, tvRoomType, tvRoomPrice, tvRoomRating;
        ImageButton btnFavorite;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            tvRoomRating = itemView.findViewById(R.id.tvRoomRating);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}