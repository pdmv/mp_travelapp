package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

public class Category {
    private String Id, ImagePath, Name;

    public Category() {
    }

    public Category(String id, String imagePath, String name) {
        Id = id;
        ImagePath = imagePath;
        Name = name;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return Name;
    }
}
