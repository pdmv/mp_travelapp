package com.mp.travel_app.Activity.User;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mp.travel_app.Adapter.TicketAdapter;
import com.mp.travel_app.Domain.Ticket;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ActivityTicketManagementBinding;

import java.util.ArrayList;
import java.util.List;

public class TicketManagementActivity extends AppCompatActivity {

    private ActivityTicketManagementBinding binding;
    private DatabaseReference database;
    private RecyclerView ticketsRecyclerView;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance().getReference("Ticket");

        ticketsRecyclerView = binding.recyclerViewTickets;
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketsRecyclerView.setAdapter(ticketAdapter);

        Common.getCurrentUser(this, new Common.GetUserCallback() {
            @Override
            public void onSuccess(Users user) {
                fetchUserTickets(user.getId());
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }


    private void fetchUserTickets(String userId) {

        database.orderByChild("customer/id").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ticketList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ticket ticket = snapshot.getValue(Ticket.class);
                    if (ticket != null) {
                        ticketList.add(ticket);
                    }
                }
                ticketAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TicketManagementActivity.this, "Failed to load tickets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
