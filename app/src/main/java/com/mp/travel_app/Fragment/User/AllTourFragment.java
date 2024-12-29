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
import com.mp.travel_app.databinding.FragmentAllTourBinding;

import java.util.ArrayList;
import java.util.List;

public class AllTourFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseDatabase database;
    DatabaseReference tourRef;

    FragmentAllTourBinding binding;

    public AllTourFragment() {
        // Required empty public constructor
    }

    public static AllTourFragment newInstance(String param1, String param2) {
        AllTourFragment fragment = new AllTourFragment();
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

        tourRef = database.getReference("Tour");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAllTourBinding.inflate(inflater, container, false);

        initTour();

        binding.allTourBackBtn.setOnClickListener(v -> back());

        return binding.getRoot();
    }

    private void initTour() {
        List<Tour> itemDomains = new ArrayList<>();

        binding.recyclerViewAllTour.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        CartAdapter popularAdapter = new CartAdapter(itemDomains);

        binding.progressBarAllTour.setVisibility(View.VISIBLE);

        tourRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    itemDomains.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Tour data = dataSnapshot.getValue(Tour.class);
                        itemDomains.add(data);
                    }

                    if (!itemDomains.isEmpty()) {
                        binding.recyclerViewAllTour.setLayoutManager(new LinearLayoutManager(
                                binding.recyclerViewAllTour.getContext(),
                                RecyclerView.VERTICAL,
                                false
                        ));

                        popularAdapter.notifyDataSetChanged();
                        binding.recyclerViewAllTour.setAdapter(popularAdapter);
                    }

                    binding.progressBarAllTour.setVisibility(View.GONE);
                    binding.noDataTxt.setVisibility(View.GONE);
                } else {
                    binding.progressBarAllTour.setVisibility(View.GONE);
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void back() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            requireActivity().finish();
        } else {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }
}