package com.mp.travel_app.Fragment.User;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private final List<Tour> itemDomains = new ArrayList<>();
    private final List<Tour> filteredItem = new ArrayList<>();
    private RecommendedAdapter filteredRecommendedAdapter;
    private PopularAdapter filteredPopularAdapter;

    FragmentHomeBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference popularRef, locationRef, bannerRef, categoryRef, itemRef;
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
        popularRef = database.getReference("Populars");
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
                filterDataByLocation(selectedLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.searchBtn.setOnClickListener(v -> {
            String query = binding.searchBox.getText().toString().trim();
            filterBySearch(query);
        });

        binding.recommendedSeeAllBtn.setOnClickListener(v -> toAllTour());
        binding.popularSeeAllBtn.setOnClickListener(v -> toAllTour());

        return binding.getRoot();
    }

    private void initPopular() {
        PopularAdapter popularAdapter = new PopularAdapter(itemDomains);

        LoadData.loadDataIntoRecyclerView(popularRef, binding.recyclerViewPopular, binding.progressBarPopular,
                binding.noDataPopularTxt, Tour.class, popularAdapter, itemDomains);
    }

    private void initRecommended() {
        RecommendedAdapter recommendedAdapter = new RecommendedAdapter(itemDomains);

        LoadData.loadDataIntoRecyclerView(itemRef, binding.recyclerViewRecommended, binding.progressBarRecommended,
                binding.noDataRecommendedTxt, Tour.class, recommendedAdapter, itemDomains);
    }

    private void initCategory() {
        List<Category> categories = new ArrayList<>();

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);

        categoryAdapter.setOnCategorySelectedListener(new CategoryAdapter.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(Category category) {
                filterDataByCategory(category);
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

    private void filterDataByLocation(Location location) {
        filteredItem.clear();

        for (Tour itemDomain : itemDomains) {
            if (itemDomain.getLocation().getLoc().equals(location.getLoc())) {
                filteredItem.add(itemDomain);
            }
        }

        filteredRecommendedAdapter = new RecommendedAdapter(filteredItem);
        filteredPopularAdapter = new PopularAdapter(filteredItem);
        binding.recyclerViewRecommended.setAdapter(filteredRecommendedAdapter);
        binding.recyclerViewPopular.setAdapter(filteredPopularAdapter);
    }

    private void filterDataByCategory(Category category) {
        filteredItem.clear();

        for (Tour itemDomain : itemDomains) {
            if (itemDomain.getCategory().getName().equals(category.getName())) {
                filteredItem.add(itemDomain);
            }
        }

        filteredRecommendedAdapter = new RecommendedAdapter(filteredItem);
        filteredPopularAdapter = new PopularAdapter(filteredItem);
        binding.recyclerViewRecommended.setAdapter(filteredRecommendedAdapter);
        binding.recyclerViewPopular.setAdapter(filteredPopularAdapter);
    }

    private void filterBySearch(String query) {
        filteredItem.clear();

        String queryLower = query.toLowerCase();

        for (Tour itemDomain : itemDomains) {
            if (itemDomain.getDateTour().toLowerCase().contains(queryLower)
                    || itemDomain.getDuration().toLowerCase().contains(queryLower)
                    || itemDomain.getTimeTour().toLowerCase().contains(queryLower)
                    || String.valueOf(itemDomain.getPrice()).toLowerCase().contains(queryLower)
                    || itemDomain.getTourGuide().getFullname().toLowerCase().contains(queryLower)
                    || itemDomain.getTourGuide().getPhoneNumber().toLowerCase().contains(queryLower)) {
                        filteredItem.add(itemDomain);
            }
        }

        filteredRecommendedAdapter = new RecommendedAdapter(filteredItem);
        filteredPopularAdapter = new PopularAdapter(filteredItem);
        binding.recyclerViewRecommended.setAdapter(filteredRecommendedAdapter);
        binding.recyclerViewPopular.setAdapter(filteredPopularAdapter);
    }

    private void toAllTour() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.menuAllTour);
    }
}