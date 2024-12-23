package com.example.powerquest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.data.ExerciseItem
import com.example.powerquest.R

class BottomSheetAdapter(
    private val exercises: List<ExerciseItem>,
    private val onExerciseSelected: (ExerciseItem) -> Unit
) : RecyclerView.Adapter<BottomSheetAdapter.BottomSheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_exercise, parent, false)
        return BottomSheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottomSheetViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount() = exercises.size

    inner class BottomSheetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleItem: TextView = view.findViewById(R.id.title_item)
        private val titleReps: TextView = view.findViewById(R.id.title_reps)
        private val lottieAnimation: LottieAnimationView = view.findViewById(R.id.exercise_animation)
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox_item)

        fun bind(exercise: ExerciseItem) {
            // Set data untuk setiap item
            titleItem.text = exercise.title
            titleReps.text = exercise.reps
            lottieAnimation.setAnimation(exercise.animationRes)

            // Handle klik checkbox untuk memilih item
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onExerciseSelected(exercise)
                }
            }
        }
    }
}
