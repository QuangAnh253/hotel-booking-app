package com.nhom4.hotelbooking.models;

import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String roomId;
    private float rating;
    private String comment;
    private long timestamp;

    public Review() {}

    public Review(String userId, String userName, String roomId, float rating, String comment, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.roomId = roomId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getRoomId() { return roomId; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
}