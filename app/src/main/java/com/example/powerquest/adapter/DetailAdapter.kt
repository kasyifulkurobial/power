package com.example.powerquest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.data.ExerciseItem
import com.example.powerquest.R

class DetailAdapter(
    private val exercises: MutableList<ExerciseItem>,
    private val onUpdateReps: (ExerciseItem) -> Unit
) : RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_layout, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount() = exercises.size

    inner class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val listName: TextView = view.findViewById(R.id.list_name)
        private val listReps: TextView = view.findViewById(R.id.list_reps)
        private val lottieAnimation: LottieAnimationView = view.findViewById(R.id.drag_animation)
        private val buttonPlus: Button = view.findViewById(R.id.plus_button)
        private val buttonMinus: Button = view.findViewById(R.id.minus_button)

        fun bind(exercise: ExerciseItem) {
            listName.text = exercise.title
            listReps.text = exercise.reps
            lottieAnimation.setAnimation(exercise.animationRes)

            buttonPlus.setOnClickListener {
                exercise.reps = adjustReps(exercise.reps, increment = true)
                listReps.text = exercise.reps
                onUpdateReps(exercise)
            }

            buttonMinus.setOnClickListener {
                exercise.reps = adjustReps(exercise.reps, increment = false)
                listReps.text = exercise.reps
                onUpdateReps(exercise)
            }
        }

        private fun adjustReps(reps: String, increment: Boolean): String {
            return if (reps.startsWith("x")) {
                // Repetisi
                val currentReps = reps.removePrefix("x").toIntOrNull() ?: 1
                val newReps = currentReps + if (increment) 1 else -1
                "x${newReps.coerceAtLeast(1)}"
            } else if (reps.contains("Detik", ignoreCase = true)) {
                // Timer
                val currentSeconds = reps.filter { it.isDigit() }.toIntOrNull() ?: 0
                val newSeconds = currentSeconds + if (increment) 5 else -5
                "${newSeconds.coerceAtLeast(5)} Detik"
            } else {
                reps // Format tidak dikenal, tidak diubah
            }
        }
    }
}
