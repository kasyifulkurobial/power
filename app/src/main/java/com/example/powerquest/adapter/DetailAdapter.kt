package com.example.powerquest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.R
import com.example.powerquest.data.ExerciseItem

class DetailAdapter(
    private val exercises: List<ExerciseItem>,
    private val onExerciseUpdated: (ExerciseItem) -> Unit
) : RecyclerView.Adapter<DetailAdapter.ExerciseViewHolder>() {

    // Peta untuk mencocokkan nama file JSON dengan ID sumber daya
    private val animationResMap = mapOf(
        "box_jump.json" to R.raw.box_jump,
        "bumper.json" to R.raw.bumper,
        "burpees.json" to R.raw.burpees,
        "chair_stand.json" to R.raw.chair_stand,
        "cobra.json" to R.raw.cobras,
        "frog_press.json" to R.raw.frog_press,
        "high_knees.json" to R.raw.high_knees,
        "inchworm.json" to R.raw.inchworm,
        "jumping_jack.json" to R.raw.jumping_jack,
        "jumping_squats.json" to R.raw.jumping_squats,
        "leg_up.json" to R.raw.leg_up,
        "press_up.json" to R.raw.press_up,
        "pull_up.json" to R.raw.pull_up,
        "punches.json" to R.raw.punches,
        "push_up.json" to R.raw.push_up,
        "reverse_crunches.json" to R.raw.reverse_crunches,
        "rope.json" to R.raw.rope,
        "run.json" to R.raw.run,
        "single_leg_hip.json" to R.raw.single_leg_hip,
        "sit_up.json" to R.raw.sit_up,
        "split_jump.json" to R.raw.split_jump,
        "squat_kicks.json" to R.raw.squat_kicks,
        "squat_reach.json" to R.raw.squat_reach
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_layout, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount() = exercises.size

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textItem: TextView = view.findViewById(R.id.list_name)
        private val textReps: TextView = view.findViewById(R.id.list_reps)
        private val lottieAnimation: LottieAnimationView = view.findViewById(R.id.drag_animation)

        fun bind(exercise: ExerciseItem) {
            textItem.text = exercise.title
            textReps.text = exercise.reps

            // Ambil ID raw dari peta
            val rawResId = animationResMap[exercise.animationRes]
            if (rawResId != null) {
                lottieAnimation.setAnimation(rawResId)
            } else {
                // Jika tidak ditemukan, kosongkan animasi
                lottieAnimation.cancelAnimation()
                lottieAnimation.clearAnimation()
            }
        }
    }
}
