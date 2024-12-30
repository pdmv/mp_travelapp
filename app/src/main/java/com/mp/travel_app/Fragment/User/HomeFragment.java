package com.mp.travel_app.Fragment.User;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Adapter.PopularAdapter;
import com.mp.travel_app.Adapter.RecommendedAdapter;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.R;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private final List<Tour> popularTour = new ArrayList<>();
    private final List<Tour> recommendedTour = new ArrayList<>();

    FragmentHomeBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference locationRef, bannerRef, categoryRef, itemRef;
    StorageReference storageReference;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        storage = FirebaseStorage
                .getInstance("gs://travel-app-75022.firebasestorage.app");

        bannerRef = database.getReference("Banner");
        locationRef = database.getReference("Location");
        categoryRef = database.getReference("Category");
        itemRef = database.getReference("Tour");
        storageReference = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initLocation();
        initBanner();
        initCategory();
        initRecommended();
        initPopular();

        binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Location selectedLocation = (Location) parent.getItemAtPosition(position);
                filterData(selectedLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.searchBtn.setOnClickListener(v -> {
            String query = binding.searchBox.getText().toString().trim();
            filterData(query);
        });

        binding.recommendedSeeAllBtn.setOnClickListener(v -> toAllTour());
        binding.popularSeeAllBtn.setOnClickListener(v -> toAllTour());

        return binding.getRoot();
    }

    private void initPopular() {
        PopularAdapter popularAdapter = new PopularAdapter(popularTour);

        LoadData.loadFilterDataIntoRecyclerView(itemRef, binding.recyclerViewPopular, binding.progressBarPopular,
                binding.noDataPopularTxt, Tour.class, popularAdapter, popularTour, tour -> {
                    return tour.getStatus().equals("Popular");
                });
    }

    private void initRecommended() {
        RecommendedAdapter recommendedAdapter = new RecommendedAdapter(recommendedTour);

        LoadData.loadFilterDataIntoRecyclerView(itemRef, binding.recyclerViewRecommended, binding.progressBarRecommended,
                binding.noDataRecommendedTxt, Tour.class, recommendedAdapter, recommendedTour, tour -> {
                    return tour.getStatus().equals("Recommended");
                });
    }

    private void initCategory() {
        List<Category> categories = new ArrayList<>();

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);

        categoryAdapter.setOnCategorySelectedListener(new CategoryAdapter.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(Category category) {
                filterData(category);
            }
        });

        LoadData.loadDataIntoRecyclerView(categoryRef, binding.recyclerViewCategory, binding.progressBarCategory,
                binding.noDataCategoryTxt, Category.class, categoryAdapter, categories);
    }

    private void initBanner() {
        List<SliderItem> sliderItems = new ArrayList<>();

        SliderAdapter sliderAdapter = new SliderAdapter(sliderItems);

        LoadData.loadDataIntoRecyclerView(bannerRef, binding.recyclerViewBanner, binding.progressBarBanner,
                binding.noDataBannerTxt, SliderItem.class, sliderAdapter, sliderItems);
    }

    private void initLocation() {
        ArrayAdapter<Location> locationAdapter = new ArrayAdapter<>(binding.getRoot().getContext(), R.layout.sp_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        LoadData.loadDataFromDatabase(binding.getRoot().getContext(), locationRef, locationAdapter, Location.class);
        binding.locationSp.setAdapter(locationAdapter);
    }

    private void filterData(Object filterCriteria) {
        List<Tour> filteredPopular = new ArrayList<>();
        List<Tour> filteredRecommended = new ArrayList<>();

        for (Tour popular : popularTour) {
            if (isMatching(popular, filterCriteria)) {
                filteredPopular.add(popular);
            }
        }

        for (Tour recommended : recommendedTour) {
            if (isMatching(recommended, filterCriteria)) {
                filteredRecommended.add(recommended);
            }
        }

        updateAdapters(filteredPopular, filteredRecommended);
    }

    private boolean isMatching(Tour tour, Object filterCriteria) {
        if (filterCriteria instanceof Location) {
            Location location = (Location) filterCriteria;
            return tour.getLocation().getLoc().equals(location.getLoc());
        } else if (filterCriteria instanceof Category) {
            Category category = (Category) filterCriteria;
            return tour.getCategory().getName().equals(category.getName());
        } else if (filterCriteria instanceof String) {
            String query = (String) filterCriteria;
            String queryLower = query.toLowerCase();
            return tour.getDateTour().toLowerCase().contains(queryLower)
                    || tour.getDuration().toLowerCase().contains(queryLower)
                    || tour.getTimeTour().toLowerCase().contains(queryLower)
                    || String.valueOf(tour.getPrice()).toLowerCase().contains(queryLower)
                    || tour.getTourGuide().getFullname().toLowerCase().contains(queryLower)
                    || tour.getTourGuide().getPhoneNumber().toLowerCase().contains(queryLower);
        }
        return false;
    }

    private void updateAdapters(List<Tour> filteredPopular, List<Tour> filteredRecommended) {
        PopularAdapter filteredPopularAdapter = new PopularAdapter(filteredPopular);
        RecommendedAdapter filteredRecommendedAdapter = new RecommendedAdapter(filteredRecommended);
        binding.recyclerViewPopular.setAdapter(filteredPopularAdapter);
        binding.recyclerViewRecommended.setAdapter(filteredRecommendedAdapter);

        if (filteredPopular.isEmpty()) {
            binding.noDataPopularTxt.setVisibility(View.VISIBLE);
        } else {
            binding.noDataPopularTxt.setVisibility(View.GONE);
        }

        if (filteredRecommended.isEmpty()) {
            binding.noDataRecommendedTxt.setVisibility(View.VISIBLE);
        } else {
            binding.noDataRecommendedTxt.setVisibility(View.GONE);
        }
    }

    private void toAllTour() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.menuAllTour);
    }
}