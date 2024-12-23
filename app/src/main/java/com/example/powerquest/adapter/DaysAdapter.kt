package com.example.powerquest.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DaysAdapter(
    private var days: List<String>,
    private val onDaySelected: (String) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private val userUID = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        FirebaseDatabase.getInstance().reference
            .child("user_progress")
            .child(userUID)
            .child("custom_schedule")
            .child(day)
            .child("status")
            .get()
            .addOnSuccessListener { snapshot ->
                val status = snapshot.getValue(String::class.java)
                val isCompleted = status == "completed"
                holder.bind(day, isCompleted)
            }
            .addOnFailureListener {
                holder.bind(day, false)
            }

        holder.itemView.setOnClickListener {
            onDaySelected(day)
        }
    }

    override fun getItemCount() = days.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDays(newDays: List<String>) {
        days = newDays
        notifyDataSetChanged()
    }

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tTitle: TextView = view.findViewById(R.id.tTitle)
        private val imageDone: ImageView = view.findViewById(R.id.imageDone)

        fun bind(day: String, isCompleted: Boolean) {
            tTitle.text = day
            imageDone.visibility = if (isCompleted) View.VISIBLE else View.GONE
        }
    }
}
