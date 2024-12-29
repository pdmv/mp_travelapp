package com.mp.travel_app.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mp.travel_app.Fragment.Admin.AdminEditUserFragment;
import com.mp.travel_app.Domain.Users;
import com.mp.travel_app.Utils.Common;
import com.mp.travel_app.databinding.ViewholderUserListBinding;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private final List<Users> users;
    private Context context;

    public UsersAdapter(List<Users> users) { this.users = users; }
    FirebaseDatabase database;
    DatabaseReference usersRef;

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        database = FirebaseDatabase
                .getInstance("https://travel-app-75022-default-rtdb.asia-southeast1.firebasedatabase.app");

        usersRef = database.getReference("Users");

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

        Common.getFileFromFirebase(user.getAvatar(), new Common.OnGetFileListener() {
            @Override
            public void onUploadSuccess(String downloadUrl) {
                Glide.with(holder.itemView.getContext())
                        .load(downloadUrl)
                        .circleCrop()
                        .into(holder.binding.userAvatarImageView);
            }

            @Override
            public void onUploadFailed(String errorMessage) {

            }
        });

        holder.binding.userEditBtn.setOnClickListener(v -> {
            AdminEditUserFragment adminEditUserFragment = new AdminEditUserFragment(user);
            adminEditUserFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "EditUserDialog");
        });

        holder.binding.userDeleteBtn.setOnClickListener(v -> {
            showDeleteConfirmDialog(user);
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

    private void showDeleteConfirmDialog(Users user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm delete");
        builder.setMessage("Are you sure you want to delete?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(user);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void deleteUser(Users user) {
        String userId = user.getId();

        if (userId != null) {
            usersRef.child(userId).removeValue().addOnSuccessListener(aVoid -> {
                Common.showToast(context, "Deleted Successfully", Toast.LENGTH_SHORT);
            })
            .addOnFailureListener(e -> {
                Log.e("Firebase", "Error delete user: " + userId, e);
            });
        }
    }
}
