package com.mp.travel_app.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LoadData {
    public static <T> void loadDataFromDatabase(Context context, DatabaseReference databaseReference, final ArrayAdapter<T> adapter, final Class<T> dataClass) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<T> dataList = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        T data = dataSnapshot.getValue(dataClass);
                        dataList.add(data);

                        try {
                            Method getIdMethod = dataClass.getMethod("getId");
                        } catch (NoSuchMethodException e) {
                            Log.e("LoadData", "Method getId() not found for class: " + dataClass.getSimpleName(), e);
                        } catch (Exception e) {
                            Log.e("LoadData", "Error invoking getId() for class: " + dataClass.getSimpleName(), e);
                        }
                    }

                    SharedPreferences sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("max_" + dataClass.getSimpleName() + "_id", (int) snapshot.getChildrenCount());
                    editor.apply();


                    adapter.clear();
                    adapter.addAll(dataList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static <T> void loadDataIntoRecyclerView(DatabaseReference databaseReference, RecyclerView recyclerView, ProgressBar progressBar, TextView noDataTextView,
                                                    Class<T> dataClass, RecyclerView.Adapter<?> adapter, List<T> dataList) {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    dataList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        T data = dataSnapshot.getValue(dataClass);
                        dataList.add(data);
                    }

                    if (!dataList.isEmpty()) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(
                                recyclerView.getContext(),
                                RecyclerView.HORIZONTAL,
                                false
                        ));

                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    noDataTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    noDataTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static <T> void loadFilterDataIntoRecyclerView(DatabaseReference databaseReference, RecyclerView recyclerView, ProgressBar progressBar, TextView noDataTextView,
                                                    Class<T> dataClass, RecyclerView.Adapter<?> adapter, List<T> dataList, Predicate<T> filterPredicate) {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();

                if (snapshot.exists()) {
                    dataList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        T data = dataSnapshot.getValue(dataClass);

                        if (filterPredicate.test(data)) {
                            dataList.add(data);
                        }
                    }

                    if (!dataList.isEmpty()) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(
                                recyclerView.getContext(),
                                RecyclerView.HORIZONTAL,
                                false
                        ));

                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    noDataTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    noDataTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}