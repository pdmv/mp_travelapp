package com.mp.travel_app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mp.travel_app.Domain.Tour;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TourCartManager {
    private static final String PREF_NAME = "tour_cart_prefs";
    private static final String KEY_CART = "tour_cart";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public TourCartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Tour> getTours() {
        String json = sharedPreferences.getString(KEY_CART, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Tour>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveTours(List<Tour> tours) {
        String json = gson.toJson(tours);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }

    public void addTour(Tour tour) {
        List<Tour> tours = getTours();
        boolean exists = false;

        for (Tour t : tours) {
            if (t.getId().equals(tour.getId())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            tours.add(tour);
            saveTours(tours);
        }
    }

    public void removeTour(String tourId) {
        List<Tour> tours = getTours();
        tours.removeIf(tour -> tour.getId().equals(tourId));
        saveTours(tours);
    }
}
