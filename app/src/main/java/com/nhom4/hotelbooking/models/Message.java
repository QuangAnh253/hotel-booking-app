package com.nhom4.hotelbooking.models;

public class Message {
    private String id;
    private String senderId;
    private String senderRole;
    private String senderName;
    private String text;
    private long timestamp;

    public Message() {}

    public Message(String senderId, String senderRole, String senderName, String text, long timestamp) {
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}