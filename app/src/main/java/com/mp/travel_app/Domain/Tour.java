package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

public class Tour {
    private int id;
    private double price;
    private String title, description, duration, imagePath, dateTour, timeTour;
    private Users tourGuide;
    private Location location;
    private Category category;

    public Tour() {
    }

    public Tour(int id, double price, String title, String description, String duration, String imagePath, String dateTour, String timeTour, Users tourGuide, Location location, Category category) {
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

    public Users getTourGuide() {
        return tourGuide;
    }

    public void setTourGuide(Users tourGuide) {
        this.tourGuide = tourGuide;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDateTour() {
        return dateTour;
    }

    public void setDateTour(String dateTour) {
        this.dateTour = dateTour;
    }

    public String getTimeTour() {
        return timeTour;
    }

    public void setTimeTour(String timeTour) {
        this.timeTour = timeTour;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
