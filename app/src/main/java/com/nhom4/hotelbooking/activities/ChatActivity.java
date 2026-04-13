package com.nhom4.hotelbooking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbarChat;
    RecyclerView recyclerMessages;
    EditText edtMessage;
    Button btnSend;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    String currentUserId;
    String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbarChat = findViewById(R.id.toolbarChat);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        setSupportActionBar(toolbarChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hỗ trợ khách hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserName = mAuth.getCurrentUser().getEmail();

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);

        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    void loadMessages() {
        db.collection(Constants.COLLECTION_CHATS)
                .document(currentUserId)
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
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    void sendMessage() {
        String text = edtMessage.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        Message message = new Message(currentUserId, Constants.ROLE_USER, currentUserName, text, timestamp);

        db.collection(Constants.COLLECTION_CHATS)
                .document(currentUserId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(docRef -> {
                    edtMessage.setText("");
                    updateConversationInfo(text, timestamp);
                });
    }

    void updateConversationInfo(String lastMsg, long timestamp) {
        Map<String, Object> info = new HashMap<>();
        info.put("userId", currentUserId);
        info.put("userName", currentUserName);
        info.put("lastMessage", lastMsg);
        info.put("lastMessageTime", timestamp);
        info.put("unreadByAdmin", true);

        db.collection(Constants.COLLECTION_CHATS)
                .document(currentUserId)
                .set(info);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}