package com.kelp.pawangcuaca.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelp.pawangcuaca.R
import com.kelp.pawangcuaca.Model.HistorySearch

class SearchAdapter(
    private val historyList: List<HistorySearch>,
    private val context: Context,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(history: HistorySearch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val history = historyList[position]

        // Set data ke ViewHolder
        holder.locationName.text = history.locationName
        holder.weatherStatus.text = history.weatherStatus
        holder.lowTempText.text = "${history.lowTempText}°C"
        holder.highTempText.text = "${history.highTempText}°C"
        holder.mainTemp.text = "${history.mainTemp}°C"

        // Periksa apakah lokasi sudah disimpan di SharedPreferences
        val sharedPreferences = context.getSharedPreferences("SavedCities", Context.MODE_PRIVATE)
        val isSaved = sharedPreferences.contains(history.locationName)



        // Set click listener untuk item
        holder.itemView.setOnClickListener {
            listener.onItemClick(history)
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationName: TextView = itemView.findViewById(R.id.locationName)
        val weatherStatus: TextView = itemView.findViewById(R.id.weatherStatus)
        val lowTempText: TextView = itemView.findViewById(R.id.lowTempText)
        val highTempText: TextView = itemView.findViewById(R.id.highTempText)
        val mainTemp: TextView = itemView.findViewById(R.id.mainTemp)
    }
}
