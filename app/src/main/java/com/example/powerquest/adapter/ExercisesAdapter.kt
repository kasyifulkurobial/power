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

class ExercisesAdapter(
    private val exercises: List<ExerciseItem>,
    private val onItemSelected: (ExerciseItem) -> Unit
) : RecyclerView.Adapter<ExercisesAdapter.ExerciseViewHolder>() {

    private val selectedItems = mutableListOf<ExerciseItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise, selectedItems.contains(exercise))

        holder.checkboxItem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(exercise)
                onItemSelected(exercise)
            } else {
                selectedItems.remove(exercise)
            }
        }
    }

    override fun getItemCount() = exercises.size

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textItem: TextView = view.findViewById(R.id.text_item)
        private val textReps: TextView = view.findViewById(R.id.text_reps)
        private val lottieAnimation: LottieAnimationView = view.findViewById(R.id.lottie_animation)
        val checkboxItem: CheckBox = view.findViewById(R.id.checkbox_item)

        fun bind(exercise: ExerciseItem, isChecked: Boolean) {
            textItem.text = exercise.title
            textReps.text = exercise.reps
            lottieAnimation.setAnimation(exercise.animationRes)
            checkboxItem.isChecked = isChecked
        }
    }
}
