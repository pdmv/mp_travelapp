package com.mp.travel_app.Domain;

public class SliderItem {
    private String id;
    private String url;
    private Tour tour;

    public SliderItem() {
    }

    public SliderItem(String id, String url, Tour tour) {
        this.id = id;
        this.url = url;
        this.tour = tour;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
