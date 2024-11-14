package com.pdmv.travelapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.pdmv.travelapp.Domain.ItemDomain;
import com.pdmv.travelapp.R;
import com.pdmv.travelapp.databinding.ActivityDetailBinding;

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private ItemDomain item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        setVariables();
    }

    @SuppressLint("DefaultLocale")
    private void setVariables() {
        binding.detailTitle.setText(item.getTitle());
        binding.detailPrice.setText(String.format("$%d", item.getPrice()));
        binding.detailBackButton.setOnClickListener(v -> finish());
        binding.detailBed.setText(String.valueOf(item.getBed()));
        binding.detailDuration.setText(item.getDuration());
        binding.detailDistance.setText(item.getDistance());
        binding.detailDescription.setText(item.getDescription());
        binding.detailAddress.setText(item.getAddress());
        binding.detailRating.setText(String.format("%s Rating", item.getScore()));
        binding.detailRatingBar.setRating((float) item.getScore());

        Glide.with(DetailActivity.this)
                .load(item.getPic())
                .into(binding.detailPicture);

        binding.detailAddToCartBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(DetailActivity.this, ...);
        });
    }

    private void getIntentExtra() {
        item = (ItemDomain) getIntent().getSerializableExtra("item");
    }
}