package com.example.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travel_app.Activity.DetailActivity;
import com.example.travel_app.Domain.ItemDomain;
import com.example.travel_app.databinding.ViewholderRecommendedBinding;

import java.util.ArrayList;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.Viewholder> {
    private final ArrayList<ItemDomain> itemDomains;
    private Context context;
    private ViewholderRecommendedBinding binding;

    public RecommendedAdapter(ArrayList<ItemDomain> itemDomains) {
        this.itemDomains = itemDomains;
    }

    @NonNull
    @Override
    public RecommendedAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderRecommendedBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();

        return new Viewholder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecommendedAdapter.Viewholder holder, int position) {
        binding.recTitle.setText(itemDomains.get(position).getTitle());
        binding.recPrice.setText(String.format("$%d", itemDomains.get(position).getPrice()));
        binding.recAddress.setText(itemDomains.get(position).getAddress());
        binding.recScore.setText(String.format("%.1f", itemDomains.get(position).getScore()));

        Glide.with(context)
                .load(itemDomains.get(position).getPic())
                .into(binding.recPicture);

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
        public Viewholder(@NonNull ViewholderRecommendedBinding binding) {
            super(binding.getRoot());
        }
    }
}
