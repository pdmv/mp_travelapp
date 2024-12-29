package com.mp.travel_app.Domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticket implements Serializable {
    String id, stripePaymentId, status, createdAt;
    Tour tour;
    Users customer;

    public Ticket() {}

    public Ticket(String id, String stripePaymentId, String status, String createdAt, Tour tour, Users customer) {
        this.id = id;
        this.stripePaymentId = stripePaymentId;
        this.status = status;
        this.createdAt = createdAt;
        this.tour = tour;
        this.customer = customer;
    }

}
