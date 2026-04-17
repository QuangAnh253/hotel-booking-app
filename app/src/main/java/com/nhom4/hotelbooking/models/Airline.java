package com.nhom4.hotelbooking.models;

import java.io.Serializable;

public class Airline implements Serializable {
    private String id;
    private String name;
    private String logoUrl;
    private String webUrl;

    public Airline() {}

    public Airline(String name, String logoUrl, String webUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.webUrl = webUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getWebUrl() { return webUrl; }
    public void setWebUrl(String webUrl) { this.webUrl = webUrl; }
}