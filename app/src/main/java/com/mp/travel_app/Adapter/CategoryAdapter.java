package com.mp.travel_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Domain.Category;
import com.mp.travel_app.R;
import com.mp.travel_app.databinding.ViewholderCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<Category> categories;
    private int selectedPosition = 0;
    private int lastSelectedPosition = -1;
    private Context context;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(inflater, parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.binding.categoryTitle.setText(category.getName());

        Glide.with(holder.itemView.getContext())
                .load(category.getImagePath())
                .into(holder.binding.categoryPic);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                lastSelectedPosition = selectedPosition;
                selectedPosition = currentPosition;

                if (lastSelectedPosition != selectedPosition) {
                    notifyItemChanged(lastSelectedPosition);
                    notifyItemChanged(selectedPosition);
                }
            }
        });

        holder.binding.categoryTitle.setTextColor(context.getResources().getColor(R.color.white, null));

        if (selectedPosition == position) {
            holder.binding.categoryPic.setBackgroundResource(0);
            holder.binding.mainLayout.setBackgroundResource(R.drawable.blue_bg);
            holder.binding.categoryTitle.setVisibility(View.VISIBLE);
        } else {
            holder.binding.categoryPic.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.mainLayout.setBackgroundResource(0);
            holder.binding.categoryTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderCategoryBinding binding;

        public CategoryViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
