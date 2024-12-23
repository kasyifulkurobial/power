package com.example.powerquest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.data.ProfileItem
import com.example.powerquest.R

class ProfileAdapter(
    private val profileItems: List<ProfileItem>,
    private val onClick: (ProfileItem) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_layout, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = profileItems[position]
        holder.bind(item, onClick)
    }

    override fun getItemCount() = profileItems.size

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)

        fun bind(item: ProfileItem, onClick: (ProfileItem) -> Unit) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
