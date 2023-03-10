package com.belki.citybike.nearest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.belki.citybike.databinding.StationViewItemBinding
import com.belki.citybike.domain.Station


class StationListAdapter(private val stationClickListener: StationClickListener) :
    ListAdapter<Station, StationListAdapter.StationViewHolder>(DiffCallback) {


    /** * * * * * * * * * * * * * * * * * * * * *
    *  Diffcallback methods for Station items *
    * * * * * * * * * * * * * * * * * * * * *  */

    companion object DiffCallback : DiffUtil.ItemCallback<Station>() {
            override fun areItemsTheSame(oldItem: Station, newItem: Station): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Station, newItem: Station): Boolean {
                return oldItem.id == newItem.id
            }
        }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  ViewHolder implementation for StationRecyclerView  *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    class StationViewHolder private constructor(private var binding: StationViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup) = StationViewHolder(
                StationViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        fun bind(station: Station) {
            binding.station = station
            binding.executePendingBindings()
        }

    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  Creating new ViewHolder -> inflating item layout    *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StationViewHolder.from(parent)

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *  Binding item to ViewHolder, setting OnClick, binding asteroid item  *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = getItem(position)
        holder.bind(station)
        holder.itemView.setOnClickListener {
            stationClickListener.onClick(station)
        }
    }

    class StationClickListener(val clickListener: (station: Station) -> Unit) {
        fun onClick(station: Station) = clickListener(station)
    }
}

