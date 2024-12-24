package com.mp.travel_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.mp.travel_app.Activity.BaseActivity;
import com.mp.travel_app.Adapter.TicketAdapter;
import com.mp.travel_app.Domain.Ticket;
import com.mp.travel_app.Utils.LoadData;
import com.mp.travel_app.databinding.ActivityAdminTicketBinding;

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
        binding.progressBarTicket.setVisibility(View.VISIBLE);

        ArrayAdapter<Ticket> ticketAdapter = new ArrayAdapter<>(AdminTicketActivity.this, android.R.layout.simple_list_item_1);

        LoadData.loadDataFromDatabaseTest(AdminTicketActivity.this, ticketRef, ticketAdapter, Ticket.class, new LoadData.DataCallback<Ticket>() {
            @Override
            public void onDataLoaded(List<Ticket> ticketList) {
                if (!ticketList.isEmpty()) {
                    binding.recyclerViewTicket.setLayoutManager(new LinearLayoutManager(
                            AdminTicketActivity.this,
                            RecyclerView.HORIZONTAL,
                            false
                    ));

                    RecyclerView.Adapter<TicketAdapter.TicketViewHolder> ticketAdapter
                            = new TicketAdapter(ticketList);
                    binding.recyclerViewTicket.setAdapter(ticketAdapter);
                } else {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                }

                binding.progressBarTicket.setVisibility(View.GONE);
            }
        });
    }

}