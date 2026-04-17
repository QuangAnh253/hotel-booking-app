package com.nhom4.hotelbooking.models;

import java.io.Serializable;

public class Park implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private double price;

    public Park() {}

    public Park(String name, String description, String imageUrl, double price) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
}