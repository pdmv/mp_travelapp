package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Category implements Serializable {
    private String Id, ImagePath, Name;

    public Category() {
    }

    public Category(String id, String imagePath, String name) {
        Id = id;
        ImagePath = imagePath;
        Name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return Name;
    }
}
