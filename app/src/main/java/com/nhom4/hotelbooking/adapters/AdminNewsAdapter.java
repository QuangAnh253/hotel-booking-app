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
import com.nhom4.hotelbooking.models.News;

import java.util.List;

public class AdminNewsAdapter extends RecyclerView.Adapter<AdminNewsAdapter.AdminNewsViewHolder> {

    private List<News> newsList;
    private OnAdminNewsActionListener listener;

    public interface OnAdminNewsActionListener {
        void onEdit(News news);
        void onDelete(News news);
    }

    public AdminNewsAdapter(List<News> newsList, OnAdminNewsActionListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_news, parent, false);
        return new AdminNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.tvAdminNewsTitle.setText(news.getTitle());
        holder.tvAdminNewsDate.setText(news.getDate());

        Glide.with(holder.itemView.getContext())
                .load(news.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgAdminNews);

        holder.btnAdminNewsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnAdminNewsMenu);
            popup.getMenu().add(0, 1, 0, "Sửa");
            popup.getMenu().add(0, 2, 0, "Xoá");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) listener.onEdit(news);
                else if (item.getItemId() == 2) listener.onDelete(news);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class AdminNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAdminNews;
        TextView tvAdminNewsTitle, tvAdminNewsDate;
        ImageButton btnAdminNewsMenu;

        public AdminNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAdminNews = itemView.findViewById(R.id.imgAdminNews);
            tvAdminNewsTitle = itemView.findViewById(R.id.tvAdminNewsTitle);
            tvAdminNewsDate = itemView.findViewById(R.id.tvAdminNewsDate);
            btnAdminNewsMenu = itemView.findViewById(R.id.btnAdminNewsMenu);
        }
    }
}