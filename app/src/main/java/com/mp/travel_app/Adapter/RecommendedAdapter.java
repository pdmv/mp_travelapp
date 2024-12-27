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
import com.mp.travel_app.databinding.ViewholderRecommendedBinding;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.RecommendedViewHolder> {
    private final List<ItemDomain> itemDomains;
    private Context context;

    public RecommendedAdapter(List<ItemDomain> itemDomains) {
        this.itemDomains = itemDomains;
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderRecommendedBinding binding = ViewholderRecommendedBinding.inflate(inflater, parent, false);
        return new RecommendedViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecommendedViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ItemDomain itemDomain = itemDomains.get(position);
        holder.binding.recTitle.setText(itemDomains.get(position).getTitle());
        holder.binding.recPrice.setText(String.format("$%d", itemDomains.get(position).getPrice()));
        holder.binding.recAddress.setText(itemDomains.get(position).getAddress());
        holder.binding.recScore.setText(String.format("%.1f", itemDomains.get(position).getScore()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("item", itemDomains.get(position));
            context.startActivity(intent);
        });

        Common.getFileFromFirebase(itemDomain.getPic(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(context)
                        .load(downloadUrl)
                        .into(holder.binding.recPicture);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(context)
                        .load(itemDomains.get(position).getPic())
                        .into(holder.binding.recPicture);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemDomains.size();
    }

    public static class RecommendedViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderRecommendedBinding binding;

        public RecommendedViewHolder(ViewholderRecommendedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}