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
import com.mp.travel_app.databinding.FragmentCartBinding;

import java.util.ArrayList;
import java.util.List;


public class CartFragment extends Fragment {
    FragmentCartBinding binding;
    FirebaseDatabase database;
    DatabaseReference cartRef;

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

        // TODO: Get date and add to cartItems here
        cartRef = database.getReference("Tour");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);

        binding.listCart.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        CartAdapter cartAdapter = new CartAdapter(cartItems);

        binding.progressBarRecommended.setVisibility(View.VISIBLE);

        cartRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cartItems.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Tour data = dataSnapshot.getValue(Tour.class);
                        cartItems.add(data);
                    }

                    if (!cartItems.isEmpty()) {
                        binding.listCart.setLayoutManager(new LinearLayoutManager(
                                binding.listCart.getContext(),
                                RecyclerView.VERTICAL,
                                false
                        ));

                        cartAdapter.notifyDataSetChanged();
                        binding.listCart.setAdapter(cartAdapter);
                    }

                    binding.progressBarRecommended.setVisibility(View.GONE);
                    binding.progressBarRecommended.setVisibility(View.GONE);
                } else {
                    binding.progressBarRecommended.setVisibility(View.GONE);
                    binding.progressBarRecommended.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}