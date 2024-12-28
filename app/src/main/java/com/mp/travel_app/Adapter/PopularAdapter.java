package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.DetailActivity;
import com.mp.travel_app.Domain.ItemDomain;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ViewholderPopularBinding;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {
    private final List<ItemDomain> itemDomains;
    private Context context;

    public PopularAdapter(List<ItemDomain> itemDomains) {
        this.itemDomains = itemDomains;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderPopularBinding binding = ViewholderPopularBinding.inflate(inflater, parent, false);
        return new PopularViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ItemDomain itemDomain = itemDomains.get(position);
        holder.binding.popularTitle.setText(itemDomains.get(position).getTitle());
        holder.binding.popularPrice.setText(String.format("$%d", itemDomains.get(position).getPrice()));
        holder.binding.popularAddress.setText(itemDomains.get(position).getAddress());
        holder.binding.popularScore.setText(String.format("%.1f", itemDomains.get(position).getScore()));

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("item", itemDomains.get(position));
            context.startActivity(intent);
        });

        Common.getFileFromFirebase(itemDomain.getPic(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(context)
                        .load(downloadUrl)
                        .into(holder.binding.popularPicture);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(context)
                        .load(itemDomains.get(position).getPic())
                        .into(holder.binding.popularPicture);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemDomains.size();
    }

    public static class PopularViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderPopularBinding binding;

        public PopularViewHolder(ViewholderPopularBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
