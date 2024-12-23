package com.example.powerquest.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.R
import com.example.powerquest.adapter.EditAdapter
import com.example.powerquest.data.ExerciseItem

class DetailEdit : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EditAdapter
    private var exercises: ArrayList<ExerciseItem>? = null
    private val availableExercises = listOf(
        ExerciseItem("Box Jump", R.raw.box_jump, "x15"),
        ExerciseItem("Bumper", R.raw.bumper, "x10"),
        ExerciseItem("Burpees", R.raw.burpees, "x15"),
        ExerciseItem("Chair Stand", R.raw.chair_stand, "x10"),
        ExerciseItem("Cobra", R.raw.cobras, "x15"),
        ExerciseItem("Frog Press", R.raw.frog_press, "x10"),
        ExerciseItem("High Knee", R.raw.high_knees, "x15"),
        ExerciseItem("Inchworm", R.raw.inchworm, "10"),
        ExerciseItem("Jumping Jack", R.raw.jumping_jack, "x15"),
        ExerciseItem("Jumping Squats", R.raw.jumping_squats, "x10"),
        ExerciseItem("Leg Up", R.raw.leg_up, "x15"),
        ExerciseItem("Press Up", R.raw.press_up, "x10"),
        ExerciseItem("Pull Up", R.raw.pull_up, "x15"),
        ExerciseItem("Punches", R.raw.punches, "x10"),
        ExerciseItem("Push Up", R.raw.push_up, "x15"),
        ExerciseItem("Reverse Crunches", R.raw.reverse_crunches, "x10"),
        ExerciseItem("Rope", R.raw.rope, "x15"),
        ExerciseItem("Sprint", R.raw.run, "15 Detik"),
        ExerciseItem("Single Leg Hip", R.raw.single_leg_hip, "30 Detik"),
        ExerciseItem("Sit Up", R.raw.sit_up, "10"),
        ExerciseItem("Split Jump", R.raw.split_jump, "x15"),
        ExerciseItem("Squat Kick", R.raw.squat_kicks, "x10"),
        ExerciseItem("Squat Reach", R.raw.squat_reach, "x15")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_edit)

        recyclerView = findViewById(R.id.edit_recycler_view)
        saveButton = findViewById(R.id.update_custom)

        // Ambil data dari Intent
        exercises = intent.getParcelableArrayListExtra("selected_exercises")

        setupRecyclerView()

        // Kembalikan data ke DetailCustom saat tombol Simpan diklik
        saveButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putParcelableArrayListExtra("selected_exercises", exercises)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = EditAdapter(this, exercises ?: mutableListOf(), availableExercises) { index, updatedExercise ->
            // Perbarui data latihan
            exercises?.set(index, updatedExercise)
            adapter.notifyItemChanged(index) // Refresh item yang diubah
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
