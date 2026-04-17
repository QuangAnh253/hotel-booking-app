package com.nhom4.hotelbooking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.models.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    List<Message> messageList;
    String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat fullFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        }
        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String timeStr = timeFormat.format(new Date(message.getTimestamp()));

        TextView tvHeaderDate;
        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.tvMessageSent.setText(message.getText());
            sentHolder.tvTimeSent.setText(timeStr);
            tvHeaderDate = sentHolder.tvChatHeaderDate;
        } else {
            ReceivedViewHolder receivedHolder = (ReceivedViewHolder) holder;
            receivedHolder.tvMessageReceived.setText(message.getText());
            receivedHolder.tvTimeReceived.setText(timeStr);
            tvHeaderDate = receivedHolder.tvChatHeaderDate;
        }

        // Xử lý hiển thị Header Date
        if (shouldShowHeader(position)) {
            tvHeaderDate.setVisibility(View.VISIBLE);
            tvHeaderDate.setText(getFormattedDate(message.getTimestamp()));
        } else {
            tvHeaderDate.setVisibility(View.GONE);
        }
    }

    private boolean shouldShowHeader(int position) {
        if (position == 0) return true;

        Message currentMsg = messageList.get(position);
        Message prevMsg = messageList.get(position - 1);

        // Hiển thị nếu cách nhau hơn 30 phút
        long diff = currentMsg.getTimestamp() - prevMsg.getTimestamp();
        return diff > (30 * 60 * 1000); 
    }

    private String getFormattedDate(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar msgDate = Calendar.getInstance();
        msgDate.setTimeInMillis(timestamp);

        if (now.get(Calendar.DATE) == msgDate.get(Calendar.DATE)) {
            return "Hôm nay";
        } else if (now.get(Calendar.DATE) - msgDate.get(Calendar.DATE) == 1) {
            return "Hôm qua";
        } else {
            return dateFormat.format(new Date(timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageSent, tvTimeSent, tvChatHeaderDate;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
            tvTimeSent = itemView.findViewById(R.id.tvTimeSent);
            tvChatHeaderDate = itemView.findViewById(R.id.tvChatHeaderDate);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageReceived, tvTimeReceived, tvChatHeaderDate;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageReceived = itemView.findViewById(R.id.tvMessageReceived);
            tvTimeReceived = itemView.findViewById(R.id.tvTimeReceived);
            tvChatHeaderDate = itemView.findViewById(R.id.tvChatHeaderDate);
        }
    }
}
