package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.Ticket;
import com.mp.travel_app.databinding.ViewholderTicketListBinding;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private final List<Ticket> tickets;
    private Context context;

    public TicketAdapter(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderTicketListBinding binding = ViewholderTicketListBinding.inflate(inflater, parent, false);
        return new TicketViewHolder(binding);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        String createdAt = ticket.getCreatedAt();
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(createdAt);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        holder.binding.txtTicketTourDuration.setText(ticket.getTour().getDuration());
        holder.binding.txtTicketCreatedAt.setText(offsetDateTime.format(formatter));
        holder.binding.txtTicketId.setText(String.valueOf(ticket.getId()));
        holder.binding.txtTicketTourLocation.setText(ticket.getTour().getLocation().getLoc());
        holder.binding.txtTicketPrice.setText(String.format("$%.2f", ticket.getTour().getPrice()));
        holder.binding.txtTicketDate.setText("Date Tour: " + ticket.getTour().getDateTour());
        holder.binding.txtTicketTime.setText(ticket.getTour().getTimeTour());
        holder.binding.txtTicketCustomerName.setText(ticket.getCustomer().getFullname());
        holder.binding.txtTicketCustomerPhone.setText(ticket.getCustomer().getPhoneNumber());
        holder.binding.txtTicketCustomerEmail.setText(ticket.getCustomer().getEmail());

        Glide.with(holder.itemView.getContext())
                .load(ticket.getTour().getImagePath())
                .into(holder.binding.imageViewTour);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTicketListBinding binding;

        public TicketViewHolder(ViewholderTicketListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
