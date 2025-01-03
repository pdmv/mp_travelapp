package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ViewholderTourBinding;

import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {
    private final List<Tour> tours;

    public TourAdapter(List<Tour> tours) {
        this.tours = tours;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderTourBinding binding = ViewholderTourBinding.inflate(inflater, parent, false);
        return new TourViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tours.get(position);
        holder.binding.tourTitle.setText(tour.getTitle());
        holder.binding.tourDuration.setText(tour.getDuration());
        holder.binding.tourLocation.setText(tour.getLocation().getLoc());
        holder.binding.tourDate.setText(tour.getDateTour());
        holder.binding.tourCategory.setText(tour.getCategory().getName());
        holder.binding.tourPrice.setText(String.format("$%.2f", tour.getPrice()));
        holder.binding.tourTime.setText(tour.getTimeTour());
        holder.binding.tourGuideFullname.setText(tour.getTourGuide().getFullname());
        holder.binding.tourStatus.setText(tour.getStatus());

        Common.getFileFromFirebase(tour.getImagePath(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(holder.itemView.getContext())
                        .load(downloadUrl)
                        .into(holder.binding.tourPicture);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(holder.itemView.getContext())
                        .load(tour.getImagePath())
                        .into(holder.binding.tourPicture);
            }
        });

        Common.getFileFromFirebase(tour.getTourGuide().getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(holder.itemView.getContext())
                        .load(downloadUrl).circleCrop()
                        .into(holder.binding.tourGuideAvatar);
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Glide.with(holder.itemView.getContext())
                        .load(tour.getTourGuide().getAvatar()).circleCrop()
                        .into(holder.binding.tourGuideAvatar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTourBinding binding;

        public TourViewHolder(ViewholderTourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
