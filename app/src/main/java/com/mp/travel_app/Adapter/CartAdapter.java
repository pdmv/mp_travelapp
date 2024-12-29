package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.TicketActivity;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ViewholderCartBinding;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<Tour> list;
    private Context context;

    public CartAdapter(List<Tour> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        ViewholderCartBinding binding = ViewholderCartBinding.inflate(inflater, parent, false);

        return new CartAdapter.CartViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Tour itemDomain = list.get(position);

        holder.binding.cartTitle.setText(list.get(position).getTitle());
        holder.binding.cartPrice.setText(String.format("$%.1f", list.get(position).getPrice()));
        holder.binding.cartAddress.setText(list.get(position).getLocation().getLoc());
        holder.binding.cartScore.setText("5");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketActivity.class);
            intent.putExtra("item", itemDomain);
            context.startActivity(intent);
        });

        Common.getFileFromFirebase(itemDomain.getImagePath(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(context)
                        .load(downloadUrl)
                        .into(holder.binding.cartPicture);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(context)
                        .load(list.get(position).getImagePath())
                        .into(holder.binding.cartPicture);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderCartBinding binding;
        public CartViewHolder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
