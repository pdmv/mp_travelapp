package com.mp.travel_app.Domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SliderItem implements Serializable {
    private String id, url;
    private Tour tour;

    public SliderItem() {
    }

    public SliderItem(String id, String url, Tour tour) {
        this.id = id;
        this.url = url;
        this.tour = tour;
    }

}
