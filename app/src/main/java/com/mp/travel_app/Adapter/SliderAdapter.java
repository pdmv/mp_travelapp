package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.SliderItem;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ViewholderBannerBinding;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private final List<SliderItem> sliderItems;

    public SliderAdapter(List<SliderItem> sliderItems) {
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderBannerBinding binding = ViewholderBannerBinding.inflate(inflater, parent, false);
        return new SliderViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        SliderItem sliderItem = sliderItems.get(position);

        Common.getFileFromFirebase(sliderItem.getUrl(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(holder.itemView.getContext())
                        .load(downloadUrl)
                        .into(holder.binding.bannerPic);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(holder.itemView.getContext())
                        .load(sliderItem.getUrl())
                        .into(holder.binding.bannerPic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderBannerBinding binding;

        public SliderViewHolder(ViewholderBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
