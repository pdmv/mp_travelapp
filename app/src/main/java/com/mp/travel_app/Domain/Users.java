package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.Data;

@Data
public class Users implements Serializable {
    private String id, fullname, phoneNumber, email, avatar, username, password, role, stripeCustomerId;

    @NonNull
    @Override
    public String toString() {
        return fullname + " - " + phoneNumber;
    }
}