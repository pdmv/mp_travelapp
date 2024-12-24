package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class Users {
    private int id;
    private String fullname, phoneNumber, email, avatar, username, password, role;

    @NonNull
    @Override
    public String toString() {
        return id + " - " + fullname;
    }
}