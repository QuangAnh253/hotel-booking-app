package com.nhom4.hotelbooking.models;

import java.io.Serializable;

public class News implements Serializable {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String content;
    private String date;

    public News() {}

    public News(String title, String description, String imageUrl, String date) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getContent() { return content; }
    public String getDate() { return date; }
}