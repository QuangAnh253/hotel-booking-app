package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom4.hotelbooking.R;
import com.nhom4.hotelbooking.adapters.MessageAdapter;
import com.nhom4.hotelbooking.models.Message;
import com.nhom4.hotelbooking.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminChatDetailActivity extends AppCompatActivity {

    Toolbar toolbarAdminChat;
    TextView tvAdminChatTitle;
    RecyclerView recyclerAdminMessages;
    EditText edtAdminMessage;
    ImageButton btnAdminSend;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    String targetUserId;
    String targetUserName;
    String adminId;
    String adminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_detail);

        // Ánh xạ View
        toolbarAdminChat = findViewById(R.id.toolbarAdminChat);
        tvAdminChatTitle = findViewById(R.id.tvAdminChatTitle);
        recyclerAdminMessages = findViewById(R.id.recyclerAdminMessages);
        edtAdminMessage = findViewById(R.id.edtAdminMessage);
        btnAdminSend = findViewById(R.id.btnAdminSend);

        // Nhận thông tin khách hàng từ Intent
        targetUserId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        targetUserName = getIntent().getStringExtra(Constants.EXTRA_USER_NAME);

        // Thiết lập Toolbar
        setSupportActionBar(toolbarAdminChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // HIỂN THỊ TÊN KHÁCH HÀNG TRÊN TIÊU ĐỀ
        if (targetUserName != null) {
            tvAdminChatTitle.setText("Hỗ trợ: " + targetUserName);
        } else {
            tvAdminChatTitle.setText("Hỗ trợ khách hàng");
        }

        findViewById(R.id.btnBackAdminChat).setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        adminId = mAuth.getCurrentUser().getUid();
        adminName = "Admin";

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, adminId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerAdminMessages.setLayoutManager(layoutManager);
        recyclerAdminMessages.setAdapter(messageAdapter);

        loadMessages();
        markAsRead();

        btnAdminSend.setOnClickListener(v -> sendMessage());
    }

    void loadMessages() {
        db.collection(Constants.COLLECTION_CHATS)
                .document(targetUserId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null || querySnapshots == null) return;

                    messageList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Message message = doc.toObject(Message.class);
                        message.setId(doc.getId());
                        messageList.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerAdminMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    void sendMessage() {
        String text = edtAdminMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        long timestamp = System.currentTimeMillis();
        Message message = new Message(adminId, Constants.ROLE_ADMIN, adminName, text, timestamp);

        db.collection(Constants.COLLECTION_CHATS)
                .document(targetUserId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(docRef -> {
                    edtAdminMessage.setText("");
                    updateConversationInfo(text, timestamp);
                });
    }

    void updateConversationInfo(String lastMsg, long timestamp) {
        Map<String, Object> info = new HashMap<>();
        info.put("lastMessage", lastMsg);
        info.put("lastMessageTime", timestamp);
        info.put("unreadByAdmin", false);
        db.collection(Constants.COLLECTION_CHATS).document(targetUserId).update(info);
    }

    void markAsRead() {
        db.collection(Constants.COLLECTION_CHATS).document(targetUserId).update("unreadByAdmin", false);
    }
}
