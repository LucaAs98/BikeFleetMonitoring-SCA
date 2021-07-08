package com.example.bikefleetmonitoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterBikeDetails extends RecyclerView.Adapter<AdapterBikeDetails.ViewHolder> {
    private ArrayList<BikeDetails> bikeDetailsList;

    public AdapterBikeDetails(ArrayList<BikeDetails> bikeDetailsList) {
        this.bikeDetailsList = bikeDetailsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.book_bike_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBikeDetails.ViewHolder holder, int position) {
        holder.tvName.setText(bikeDetailsList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return bikeDetailsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.bike_name);
        }
    }
}
