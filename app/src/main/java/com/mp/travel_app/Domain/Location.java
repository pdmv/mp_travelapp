package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

public class Location {
    private String Id, loc;

    public Location() {
    }

    public Location(String id, String loc) {
        this.Id = id;
        this.loc = loc;
    }

    public String getId() { return Id; }

    public void setId(String id) {
        this.Id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    @NonNull
    @Override
    public String toString() {
        return this.loc;
    }
}
