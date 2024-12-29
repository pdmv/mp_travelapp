package com.mp.travel_app.Fragment.User;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Adapter.CartAdapter;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.R;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.Utils.TourCartManager;
import com.mp.travel_app.databinding.FragmentCartBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CartFragment extends Fragment {
    private TourCartManager cartManager;
    FragmentCartBinding binding;
    FirebaseDatabase database;

    private List<Tour> cartItems = new ArrayList<>();

    public CartFragment() {
    }

    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        cartManager = new TourCartManager(requireContext());

        cartItems = cartManager.getTours();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);

        binding.listCart.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        CartAdapter cartAdapter = new CartAdapter(cartItems);

        binding.progressBarRecommended.setVisibility(View.VISIBLE);

        if (!cartItems.isEmpty()) {
            binding.listCart.setLayoutManager(new LinearLayoutManager(
                    binding.listCart.getContext(),
                    RecyclerView.VERTICAL,
                    false
            ));

            binding.listCart.setAdapter(cartAdapter);
        }

        binding.progressBarRecommended.setVisibility(View.GONE);

        return binding.getRoot();
    }
}