package com.mp.travel_app.Activity.User;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        // Inflating layout bằng ViewBinding
        binding = ActivityTicketManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Database Reference
        database = FirebaseDatabase.getInstance().getReference("Tickets");

        // Khởi tạo RecyclerView và Adapter
        ticketsRecyclerView = binding.recyclerViewTickets;
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketsRecyclerView.setAdapter(ticketAdapter);

        String userId = "example_user_id";
        fetchUserTickets(userId);
    }


    private void fetchUserTickets(String userId) {

        database.orderByChild("customer/id").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TicketManagementActivity.this, "Failed to load tickets: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
