package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class Users {
    private String id, fullname, phoneNumber, email, avatar, username, password, role;

    @NonNull
    @Override
    public String toString() {
        return id + " - " + fullname;
    }
}