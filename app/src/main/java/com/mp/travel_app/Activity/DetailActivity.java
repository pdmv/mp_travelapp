package com.mp.travel_app.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.Tour;
import com.mp.travel_app.databinding.ActivityDetailBinding;

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private Tour item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        setVariables();
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setVariables() {
        binding.detailTitle.setText(item.getTitle());
        binding.detailPrice.setText(String.format("$%.1f", item.getPrice()));
        binding.detailBackButton.setOnClickListener(v -> finish());
        binding.detailBed.setText("2");
        binding.detailDuration.setText(item.getDuration());
        binding.detailDistance.setText("100 km");
        binding.detailDescription.setText(item.getDescription());
        binding.detailAddress.setText(item.getLocation().getLoc());
        binding.detailRating.setText("5 Rating");
        binding.detailRatingBar.setRating(5.0F);

        Glide.with(DetailActivity.this)
                .load(item.getImagePath())
                .into(binding.detailPicture);

        binding.detailAddToCartBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, TicketActivity.class);
            intent.putExtra("object", item);
            startActivity(intent);
        });
    }

    private void getIntentExtra() {
        item = (Tour) getIntent().getSerializableExtra("item");
    }
}