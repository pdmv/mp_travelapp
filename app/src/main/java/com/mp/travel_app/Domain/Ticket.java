package com.mp.travel_app.Domain;

import androidx.annotation.NonNull;

public class Ticket {
    int id;
    String createdAt;
    Tour tour;
    Customer customer;

    public Ticket() {}

    public Ticket(int id, String createdAt, Tour tour, Customer customer) {
        this.id = id;
        this.createdAt = createdAt;
        this.tour = tour;
        this.customer = customer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
