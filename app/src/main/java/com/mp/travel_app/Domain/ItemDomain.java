package com.mp.travel_app.Domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDomain implements Serializable {
    private String address;
    private int bed;
    private String dateTour;
    private String description;
    private String distance;
    private String duration;
    private String pic;
    private int price;
    private double score;
    private String timeTour;
    private String title;
    private String tourGuideName;
    private String tourGuidePhone;
    private String tourGuidePic;

    public ItemDomain() {
    }

}
