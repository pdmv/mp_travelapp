package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tour {
    private double price;
    private String id, title, description, duration, imagePath, dateTour, timeTour;
    private Users tourGuide;
    private Location location;
    private Category category;

    public Tour() {
    }

    public Tour(String id, double price, String title, String description, String duration, String imagePath, String dateTour, String timeTour, Users tourGuide, Location location, Category category) {
        this.id = id;
        this.price = price;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.imagePath = imagePath;
        this.dateTour = dateTour;
        this.timeTour = timeTour;
        this.tourGuide = tourGuide;
        this.location = location;
        this.category = category;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
