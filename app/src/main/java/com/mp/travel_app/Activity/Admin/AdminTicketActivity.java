package com.mp.travel_app.Activity.Admin;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.TicketAdapter;
import com.mp.travel_app.Domain.Ticket;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminTicketBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminTicketActivity extends BaseActivity {
    ActivityAdminTicketBinding binding;
    DatabaseReference ticketRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminTicketBinding.inflate(getLayoutInflater());
        ticketRef = database.getReference("Ticket");

        setContentView(binding.getRoot());

        initTicket();

        binding.ticketBackBtn.setOnClickListener(v -> finish());
    }

    private void initTicket() {
        List<Ticket> tickets = new ArrayList<>();

        TicketAdapter ticketAdapter = new TicketAdapter(tickets);

        LoadData.loadDataIntoRecyclerView(ticketRef, binding.recyclerViewTicket, binding.progressBarTicket,
                binding.noDataTxt, Ticket.class, ticketAdapter, tickets);
    }

}