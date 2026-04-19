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
import com.nhom4.hotelbooking.activities.NewsDetailActivity;
import com.nhom4.hotelbooking.models.News;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> newsList;
    private boolean isHorizontal;

    public NewsAdapter(List<News> newsList, boolean isHorizontal) {
        this.newsList = newsList;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isHorizontal ? R.layout.item_news_horizontal : R.layout.item_news_vertical;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.tvTitle.setText(news.getTitle());
        if (holder.tvDate != null) holder.tvDate.setText(news.getDate());
        if (holder.tvDesc != null) holder.tvDesc.setText(news.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(news.getImageUrl())
                .placeholder(R.mipmap.logo_hotel)
                .into(holder.imgNews);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), NewsDetailActivity.class);
            intent.putExtra(Constants.EXTRA_NEWS, news);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvTitle, tvDate, tvDesc;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvDate = itemView.findViewById(R.id.tvNewsDate);
            tvDesc = itemView.findViewById(R.id.tvNewsDesc);
        }
    }
}