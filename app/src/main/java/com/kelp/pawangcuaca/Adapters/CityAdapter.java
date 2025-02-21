package com.kelp.pawangcuaca.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kelp.pawangcuaca.Activitis.SearchActivity;
import com.kelp.pawangcuaca.Model.City;
import com.kelp.pawangcuaca.R;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private final List<City> cityList;
    private final SearchActivity searchActivity;

    private OnAddCityClickListener addCityClickListener;

    // Interface untuk klik tambah kota
    public interface OnAddCityClickListener {
        void onAddCityClick(City city);
    }

    public CityAdapter(List<City> cityList, SearchActivity searchActivity) {
        this.cityList = cityList;
        this.searchActivity = searchActivity;
    }

    // Setter untuk listener add city
    public void setOnAddCityClickListener(OnAddCityClickListener listener) {
        this.addCityClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menginflate layout item_city
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        City city = cityList.get(position);
        holder.cityName.setText(city.getName());

        // Mengecek apakah kota sudah disimpan di SharedPreferences
        SharedPreferences sharedPreferences = searchActivity.getSharedPreferences("SavedCities", Context.MODE_PRIVATE);
        boolean citySaved = sharedPreferences.contains(city.getName());

        // Menandai jika kota sudah disimpan di SharedPreferences
        if (citySaved) {
            holder.addkota.setVisibility(View.GONE);  // Hide add button jika sudah disimpan
        } else {
            holder.addkota.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView cityName;
        ImageView addkota;
        ImageView deletekota;

        public ViewHolder(View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            addkota = itemView.findViewById(R.id.addkota);
            deletekota = itemView.findViewById(R.id.deletekota);

            // Listener untuk klik addkota (menambahkan kota)
            addkota.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && addCityClickListener != null) {
                        City city = cityList.get(position);
                        // Menyimpan kota ke SharedPreferences
                        saveCity(city);
                        addCityClickListener.onAddCityClick(city);
                    }
                }
            });

            // Listener untuk klik deletekota (menghapus kota)
            deletekota.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        City city = cityList.get(position);

                        // Menghapus kota dari list dan memperbarui RecyclerView
                        cityList.remove(position);
                        notifyItemRemoved(position);

                        // Menghapus kota dari SharedPreferences
                        removeCityFromStorage(city);

                        // Memberikan feedback kepada pengguna
                        Toast.makeText(searchActivity, city.getName() + " dihapus", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Listener untuk klik item (misalnya melihat cuaca kota)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        City city = cityList.get(position);

                        // Tampilkan informasi kota dengan Toast (bisa diubah jadi dialog)
                        Toast.makeText(searchActivity, "Kota dipilih: " + city.getName(), Toast.LENGTH_SHORT).show();

                        // Menampilkan detail cuaca kota
                        searchActivity.showCityDetails(city);
                    }
                }
            });
        }

        // Fungsi untuk menyimpan kota ke SharedPreferences
        private void saveCity(City city) {
            SharedPreferences sharedPreferences = searchActivity.getSharedPreferences("SavedCities", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(city.getName(), city.getKey());  // Simpan nama dan key kota
            editor.apply();

            // Memberikan feedback kepada pengguna
            Toast.makeText(searchActivity, city.getName() + " disimpan", Toast.LENGTH_SHORT).show();
        }

        // Fungsi untuk menghapus kota dari SharedPreferences
        private void removeCityFromStorage(City city) {
            SharedPreferences sharedPreferences = searchActivity.getSharedPreferences("SavedCities", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(city.getName());  // Hapus nama kota dari SharedPreferences
            editor.apply();
        }
    }
}
