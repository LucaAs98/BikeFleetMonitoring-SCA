package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterDettagliBici extends RecyclerView.Adapter<AdapterDettagliBici.ViewHolder> {
    private ArrayList<DettagliBici> dettagliBiciList;

    public AdapterDettagliBici(ArrayList<DettagliBici> dettagliBiciList) {
        this.dettagliBiciList = dettagliBiciList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_prenota_bici,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDettagliBici.ViewHolder holder, int position) {
        holder.tvNomeBici.setText(dettagliBiciList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return dettagliBiciList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNomeBici;
        Button btnPrenotaBici;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNomeBici = itemView.findViewById(R.id.nomeBici);
            btnPrenotaBici = itemView.findViewById(R.id.btnPrenotaBici);
            btnPrenotaBici.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ConfermaPrenotazione.class);
                    v.getContext().startActivity(intent);
                }
            });
        }


    }
}
