package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom4.hotelbooking.R;

import java.util.List;
import java.util.Map;

public class ChatConversationAdapter extends RecyclerView.Adapter<ChatConversationAdapter.ViewHolder> {

    List<Map<String, Object>> conversationList;
    OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onClick(String userId, String userName);
    }

    public ChatConversationAdapter(List<Map<String, Object>> conversationList, OnConversationClickListener listener) {
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> conversation = conversationList.get(position);

        String userName = (String) conversation.get("userName");
        String lastMessage = (String) conversation.get("lastMessage");
        String time = (String) conversation.get("lastMessageTimeStr"); // Thêm thời gian nếu có
        Boolean unread = (Boolean) conversation.get("unreadByAdmin");

        holder.tvConversationUserName.setText(userName != null ? userName : "Người dùng");
        holder.tvConversationLastMessage.setText(lastMessage != null ? lastMessage : "");
        
        if (holder.tvConversationTime != null && time != null) {
            holder.tvConversationTime.setText(time);
        }

        if (Boolean.TRUE.equals(unread)) {
            holder.tvUnreadDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadDot.setVisibility(View.GONE);
        }

        String userId = (String) conversation.get("userId");
        holder.itemView.setOnClickListener(v -> listener.onClick(userId, userName));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConversationUserName, tvConversationLastMessage, tvConversationTime;
        View tvUnreadDot; // Đã đổi từ TextView thành View để khớp với layout mới

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConversationUserName = itemView.findViewById(R.id.tvConversationUserName);
            tvConversationLastMessage = itemView.findViewById(R.id.tvConversationLastMessage);
            tvConversationTime = itemView.findViewById(R.id.tvConversationTime);
            tvUnreadDot = itemView.findViewById(R.id.tvUnreadDot);
        }
    }
}
