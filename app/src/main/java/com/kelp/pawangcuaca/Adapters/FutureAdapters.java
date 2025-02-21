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
import com.kelp.pawangcuaca.Domains.FutureDomain;
import com.kelp.pawangcuaca.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FutureAdapters extends RecyclerView.Adapter<FutureAdapters.viewholder> {
    ArrayList<FutureDomain> items;
    Context context;

    public FutureAdapters(ArrayList<FutureDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FutureAdapters.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_future, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FutureAdapters.viewholder holder, int position) {
        FutureDomain currentItem = items.get(position);

        Log.d("FutureAdapters", "Processing item at position: " + position);
        Log.d("FutureAdapters", "Status: " + currentItem.getStatus());

        holder.dayTxt.setText(getFormattedDay(currentItem.getPredictionStartTime()));
        holder.statusTxt.setText(getTranslatedStatus(currentItem.getStatus()));
        holder.LowTxt.setText(currentItem.getLowTemp() + "°C");
        holder.HighTxt.setText(currentItem.getHighTemp() + "°C");

        // Mendapatkan gambar cuaca berdasarkan status cuaca
        int weatherIcon = getDrawableForWeather(currentItem.getStatus());
        holder.Pic.setImageResource(weatherIcon);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getFormattedDay(String predictionStartTime) {
        // Format tanggal yang diharapkan (ISO 8601)
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        if (predictionStartTime == null || predictionStartTime.matches("^[A-Za-z]+$")) {
            // Jika data berupa hari langsung ("Kamis", "Jumat"), gunakan langsung
            Log.w("FutureAdapters", "Direct day format detected: " + predictionStartTime);
            return predictionStartTime;
        }

        if (!predictionStartTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+")) {
            // Format tidak sesuai
            Log.e("FutureAdapters", "Invalid date format: " + predictionStartTime);
            return "Hari Tidak Diketahui";
        }

        try {
            // Parse dan format ulang tanggal
            Date date = originalFormat.parse(predictionStartTime);
            return targetFormat.format(date);
        } catch (ParseException e) {
            Log.e("FutureAdapters", "Error parsing date: " + predictionStartTime, e);
            return "Hari Tidak Diketahui";
        }
    }

    private String getTranslatedStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            Log.w("FutureAdapters", "Empty or null status received, using default");
            return "Cerah";
        }

        Log.d("FutureAdapters", "getTranslatedStatus input: " + status);
        switch (status.trim().toLowerCase()) {
            case "cloudy":
                return "Berawan";
            case "sunny":
                return "Cerah";
            case "rain":
                return "Hujan";
            case "partly cloudy":
                return "Awan Berselang";
            case "snow":
                return "Salju";
            case "thunderstorms":
                return "Badai Petir";
            case "showers":
                return "Hujan Ringan";
            case "mostly cloudy":
                return "Sebagian Berawan";
            case "sebagian berawan":
                return "Sebagian Berawan";
            case "hujan ringan":
                return "Hujan Ringan";
            case "berawan":
                return "Berawan";
            default:
                Log.e("FutureAdapters", "Unrecognized status: " + status);
                return "Cerah"; // Nilai default
        }
    }

    private int getDrawableForWeather(String status) {
        if (status == null || status.trim().isEmpty()) {
            Log.w("FutureAdapters", "Empty or null status received, using default drawable");
            return R.drawable.sunny;
        }

        Log.d("FutureAdapters", "getDrawableForWeather input: " + status);
        switch (status.trim().toLowerCase()) {
            case "cloudy":
                return R.drawable.cloudy;
            case "sunny":
                return R.drawable.sunny;
            case "rain":
                return R.drawable.rain;
            case "partly cloudy":
                return R.drawable.cloudy_sunny;
            case "thunderstorms":
                return R.drawable.storm;
            case "showers":
                return R.drawable.rainy;
            case "snow":
                return R.drawable.snowy;
            case "mostly cloudy":
                return R.drawable.cloudy_sunny;
            case "sebagian berawan":
                return R.drawable.cloudy_sunny;
            case "hujan ringan":
                return R.drawable.rainy;
            case "berawan":
                return R.drawable.cloudy;
            default:
                Log.e("FutureAdapters", "Unrecognized status: " + status);
                return R.drawable.sunny;
        }
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView dayTxt, statusTxt, LowTxt, HighTxt;
        ImageView Pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);

            dayTxt = itemView.findViewById(R.id.dayTxt);
            statusTxt = itemView.findViewById(R.id.statusTxt);
            LowTxt = itemView.findViewById(R.id.lowTxt);
            HighTxt = itemView.findViewById(R.id.highTxt);
            Pic = itemView.findViewById(R.id.pic);
        }
    }
}
