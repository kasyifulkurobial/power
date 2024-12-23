package com.example.powerquest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.R

class AnimationAdapter(
    private val animationList: List<Int>,
    private val nameList: List<String>,
    private val repList: MutableList<String>
) : RecyclerView.Adapter<AnimationAdapter.AnimationViewHolder>() {

    class AnimationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lottieView: LottieAnimationView = view.findViewById(R.id.drag_icon)
        val nameTextView: TextView = view.findViewById(R.id.exercise_name)
        val repsTextView: TextView = view.findViewById(R.id.exercise_reps)
        val plusButton: Button = view.findViewById(R.id.plus_button)
        val minusButton: Button = view.findViewById(R.id.minus_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return AnimationViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimationViewHolder, position: Int) {
        holder.lottieView.setAnimation(animationList[position])
        holder.nameTextView.text = nameList[position]
        holder.repsTextView.text = repList[position]

        holder.plusButton.setOnClickListener {
            val currentRep = repList[position]
            repList[position] = updateRepValue(currentRep, isIncrement = true)
            notifyItemChanged(position)
        }

        holder.minusButton.setOnClickListener {
            val currentRep = repList[position]
            repList[position] = updateRepValue(currentRep, isIncrement = false)
            notifyItemChanged(position)
        }
    }

    private fun updateRepValue(rep: String, isIncrement: Boolean): String {
        return if (rep.contains("Detik")) {
            // Update time-based reps
            val time = rep.replace(" Detik", "").toInt()
            val newTime = if (isIncrement) time + 5 else time - 5
            "${newTime.coerceAtLeast(0)} Detik" // Avoid negative time
        } else {
            // Update count-based reps
            val count = rep.replace("x", "").toInt()
            val newCount = if (isIncrement) count + 1 else count - 1
            "x${newCount.coerceAtLeast(0)}" // Avoid negative reps
        }
    }

    override fun getItemCount(): Int = animationList.size
}
