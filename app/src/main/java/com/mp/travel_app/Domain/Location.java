package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Location implements Serializable {
    private String Id, loc;

    public Location() {
    }

    public Location(String id, String loc) {
        this.Id = id;
        this.loc = loc;
    }

    @NonNull
    @Override
    public String toString() {
        return this.loc;
    }
}
