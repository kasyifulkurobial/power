package com.example.powerquest.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.data.ExerciseItem
import com.example.powerquest.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class EditAdapter(
    private val context: Context,
    private val exercises: MutableList<ExerciseItem>,
    private val availableExercises: List<ExerciseItem>,
    private val onExerciseUpdated: (Int, ExerciseItem) -> Unit
) : RecyclerView.Adapter<EditAdapter.EditViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_layout, parent, false)
        return EditViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise, position)
    }

    override fun getItemCount() = exercises.size

    inner class EditViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val listName: TextView = view.findViewById(R.id.list_name)
        private val listReps: TextView = view.findViewById(R.id.list_reps)
        private val lottieAnimation: LottieAnimationView = view.findViewById(R.id.drag_animation)
        private val buttonEdit: TextView = view.findViewById(R.id.btnEdit)

        fun bind(exercise: ExerciseItem, position: Int) {
            // Set data untuk item
            listName.text = exercise.title
            listReps.text = exercise.reps
            lottieAnimation.setAnimation(exercise.animationRes)

            // Handle klik tombol Edit
            buttonEdit.setOnClickListener {
                showEditBottomSheet(position)
            }
        }

        @SuppressLint("InflateParams")
        private fun showEditBottomSheet(index: Int) {
            // Membuka BottomSheet dialog
            val bottomSheetDialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_exercise, null)
            bottomSheetDialog.setContentView(view)

            // Setup RecyclerView dalam BottomSheet
            val recyclerView = view.findViewById<RecyclerView>(R.id.edit_recycler_view_exercises)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = BottomSheetAdapter(availableExercises) { selectedExercise ->
                // Memperbarui data item
                exercises[index] = selectedExercise
                notifyItemChanged(index) // Refresh tampilan item
                onExerciseUpdated(index, selectedExercise) // Callback ke aktivitas
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }
    }
}
