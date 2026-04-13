package com.nhom4.hotelbooking.models;

import java.io.Serializable;

public class Room implements Serializable {
    private String id;
    private String name;
    private String type;
    private double price;
    private String description;
    private String imageUrl;
    private int capacity;
    private String status;

    public Room() {}

    public Room(String id, String name, String type, double price,
                String description, String imageUrl, int capacity, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
