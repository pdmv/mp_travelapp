package com.mp.travel_app.Domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDomain implements Serializable {
    private int bed, price;
    private String address, dateTour, description, distance, duration, pic, timeTour, title, tourGuideName,
            tourGuidePhone, tourGuidePic, category;
    private double score;

    public ItemDomain() {
    }

}
