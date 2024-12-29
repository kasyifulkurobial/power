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

            // Gunakan nama file untuk mendapatkan ID sumber daya di folder raw
            val rawResId = getRawResIdByName(exercise.animationRes)
            if (rawResId != 0) {
                lottieAnimation.setAnimation(rawResId) // Muat animasi dari raw
            } else {
                // Jika file tidak ditemukan, beri animasi default atau kosongkan animasi
                lottieAnimation.cancelAnimation()
                lottieAnimation.clearAnimation()
            }

            checkboxItem.isChecked = isChecked
        }

        private fun getRawResIdByName(fileName: String): Int {
            // Cari ID sumber daya berdasarkan nama file tanpa ekstensi
            return itemView.context.resources.getIdentifier(
                fileName.replace(".json", ""), "raw", itemView.context.packageName
            )
        }
    }
}
