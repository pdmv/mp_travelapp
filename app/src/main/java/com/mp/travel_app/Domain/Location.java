package com.mp.travel_app.Domain;

public class Location {
    private int Id;
    private String loc;

    public Location() {};

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return this.loc;
    }
}
