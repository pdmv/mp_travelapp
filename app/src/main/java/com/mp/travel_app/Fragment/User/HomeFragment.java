package com.mp.travel_app.Fragment.User;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mp.travel_app.Adapter.CategoryAdapter;
import com.mp.travel_app.Adapter.PopularAdapter;
import com.mp.travel_app.Adapter.RecommendedAdapter;
import com.mp.travel_app.Adapter.SliderAdapter;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.Domain.ItemDomain;
import com.mp.travel_app.Domain.Location;
import com.mp.travel_app.Domain.SliderItem;
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
        itemRef = database.getReference("Item");
        popularRef = database.getReference("Popular");
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

        return binding.getRoot();
    }

    private void initPopular() {
        List<ItemDomain> itemDomains = new ArrayList<>();

        PopularAdapter popularAdapter = new PopularAdapter(itemDomains);

        LoadData.loadDataIntoRecyclerView(popularRef, binding.recyclerViewPopular, binding.progressBarPopular,
                binding.noDataPopularTxt, ItemDomain.class, popularAdapter, itemDomains);
    }

    private void initRecommended() {
        List<ItemDomain> itemDomains = new ArrayList<>();

        RecommendedAdapter recommendedAdapter = new RecommendedAdapter(itemDomains);

        LoadData.loadDataIntoRecyclerView(itemRef, binding.recyclerViewRecommended, binding.progressBarRecommended,
                binding.noDataRecommendedTxt, ItemDomain.class, recommendedAdapter, itemDomains);
    }

    private void initCategory() {
        List<Category> categories = new ArrayList<>();

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);

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
}