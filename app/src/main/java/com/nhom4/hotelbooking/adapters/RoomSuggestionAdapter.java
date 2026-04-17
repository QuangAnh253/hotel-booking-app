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
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Room;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomSuggestionAdapter extends RecyclerView.Adapter<RoomSuggestionAdapter.ViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomSuggestionAdapter(List<Room> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.tvRoomName.setText(room.getName());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvRoomPrice.setText(formatter.format(room.getPrice()));

        Glide.with(holder.itemView.getContext())
                .load(room.getImageUrl())
                .placeholder(R.mipmap.logo_hotel)
                .into(holder.imgRoom);

        checkFavoriteStatus(room.getId(), holder.btnFavorite);

        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(room, holder.btnFavorite));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRoomClick(room);
        });
    }

    private void checkFavoriteStatus(String roomId, ImageButton btn) {
        if (userId == null) return;
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("roomId", roomId)
                .get()
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
            db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("roomId", room.getId())
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (var doc : snap) doc.getReference().delete();
                        btn.setImageResource(R.drawable.ic_favorite_border);
                        btn.setTag("border");
                        Toast.makeText(btn.getContext(), "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> fav = new HashMap<>();
            fav.put("userId", userId);
            fav.put("roomId", room.getId());
            db.collection("favorites").add(fav).addOnSuccessListener(doc -> {
                btn.setImageResource(R.drawable.ic_favorite_filled);
                btn.setTag("filled");
                Toast.makeText(btn.getContext(), "Đã lưu yêu thích", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRoom;
        TextView tvRoomName, tvRoomPrice;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomPrice = itemView.findViewById(R.id.tvRoomPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
