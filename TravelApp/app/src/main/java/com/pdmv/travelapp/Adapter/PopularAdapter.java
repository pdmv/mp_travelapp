package com.pdmv.travelapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pdmv.travelapp.Activity.DetailActivity;
import com.pdmv.travelapp.Domain.ItemDomain;
import com.pdmv.travelapp.databinding.ViewholderPopularBinding;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.Viewholder> {
    private final ArrayList<ItemDomain> itemDomains;
    private Context context;
    private ViewholderPopularBinding binding;

    public PopularAdapter(ArrayList<ItemDomain> itemDomains) {
        this.itemDomains = itemDomains;
    }

    @NonNull
    @Override
    public PopularAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderPopularBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();

        return new Viewholder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.Viewholder holder, int position) {
        binding.popularTitle.setText(itemDomains.get(position).getTitle());
        binding.popularPrice.setText(String.format("$%d", itemDomains.get(position).getPrice()));
        binding.popularAddress.setText(itemDomains.get(position).getAddress());
        binding.popularScore.setText(String.format("%.1f", itemDomains.get(position).getScore()));

        Glide.with(context)
                .load(itemDomains.get(position).getPic())
                .into(binding.popularPicture);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("item", itemDomains.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemDomains.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        public Viewholder(@NonNull ViewholderPopularBinding binding) {
            super(binding.getRoot());
        }
    }
}
