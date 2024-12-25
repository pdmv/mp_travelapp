package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.travel_app.Activity.Admin.AdminEditUserFragment;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.databinding.ViewholderUserListBinding;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private final List<Users> users;
    private Context context;

    public UsersAdapter(List<Users> users) { this.users = users; }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderUserListBinding binding = ViewholderUserListBinding.inflate(inflater, parent, false);
        return new UsersViewHolder(binding);
    }

    @SuppressLint({"DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        Users user = users.get(position);

        holder.binding.userUsernameTxt.setText(user.getUsername());
        holder.binding.userRoleTxt.setText(user.getRole());
        holder.binding.userFullnameTxt.setText(user.getFullname());
        holder.binding.userPhoneNumberTxt.setText(user.getPhoneNumber());
        holder.binding.userEmailTxt.setText(user.getEmail());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatar()).circleCrop()
                .into(holder.binding.userAvatarImageView);

        holder.binding.userEditBtn.setOnClickListener(v -> {
            AdminEditUserFragment adminEditUserFragment = new AdminEditUserFragment(user);
            adminEditUserFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "EditUserDialog");
        });
    }

    @Override
    public int getItemCount() { return users.size(); }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderUserListBinding binding;

        public UsersViewHolder(ViewholderUserListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
