package com.kelp.pawangcuaca.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kelp.pawangcuaca.Domains.Hourly;
import com.kelp.pawangcuaca.R;

import java.util.ArrayList;

public class HourlyAdapters extends RecyclerView.Adapter<HourlyAdapters.ViewHolder> {
    private ArrayList<Hourly> items;
    private Context context;

    public HourlyAdapters(Context context, ArrayList<Hourly> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_hourly, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Hourly item = items.get(position);

            // Menampilkan jam
            if (item.getHour() != null) {
                holder.hourTxt.setText(item.getHour());
            }
            // Menampilkan suhu
            if (item.getTempCelsius() != null) {
                holder.tempTxt.setText(item.getTempCelsius() + "°");
            }

            // Memuat gambar ikon cuaca
            Glide.with(context)
                    .load(item.getPicPath())
                    .placeholder(R.drawable.sunny)
                    .into(holder.pic);

        } catch (NullPointerException e) {
            Log.e("HourlyAdapters", "NullPointerException: " + e.getMessage());
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(ArrayList<Hourly> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hourTxt, tempTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hourTxt = itemView.findViewById(R.id.hourTxt);
            tempTxt = itemView.findViewById(R.id.tempTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}