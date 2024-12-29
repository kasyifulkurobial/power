package com.example.powerquest.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.R
import com.example.powerquest.adapter.EditAdapter
import com.example.powerquest.data.ExerciseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailEdit : AppCompatActivity() {

    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EditAdapter
    private var exercises: ArrayList<ExerciseItem>? = null
    private var selectedDay: String? = null
    private val availableExercises = listOf(
        ExerciseItem("Box Jump", "box_jump.json", "x15"),
        ExerciseItem("Bumper", "bumper.json", "x10"),
        ExerciseItem("Burpees", "burpees.json", "x15"),
        ExerciseItem("Chair Stand", "chair_stand.json", "x10"),
        ExerciseItem("Cobra", "cobras.json", "x15"),
        ExerciseItem("Frog Press", "frog_press.json", "x10"),
        ExerciseItem("High Knee", "high_knees.json", "x15"),
        ExerciseItem("Inchworm", "inchworm.json", "x10"),
        ExerciseItem("Jumping Jack", "jumping_jack.json", "x15"),
        ExerciseItem("Jumping Squats", "jumping_squats.json", "x10"),
        ExerciseItem("Leg Up", "leg_up.json", "x15"),
        ExerciseItem("Press Up", "press_up.json", "x10"),
        ExerciseItem("Pull Up", "pull_up.json", "x15"),
        ExerciseItem("Punches", "punches.json", "x10"),
        ExerciseItem("Push Up", "push_up.json", "x15"),
        ExerciseItem("Reverse Crunches", "reverse_crunches.json", "x10"),
        ExerciseItem("Rope", "rope.json", "x15"),
        ExerciseItem("Sprint", "run.json", "15 Detik"),
        ExerciseItem("Single Leg Hip", "single_leg_hip.json", "30 Detik"),
        ExerciseItem("Sit Up", "sit_up.json", "x10"),
        ExerciseItem("Split Jump", "split_jump.json", "x15"),
        ExerciseItem("Squat Kick", "squat_kicks.json", "x10"),
        ExerciseItem("Squat Reach", "squat_reach.json", "x15")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_edit)

        recyclerView = findViewById(R.id.edit_recycler_view)
        saveButton = findViewById(R.id.update_custom)

        // Ambil data dari Intent
        exercises = intent.getParcelableArrayListExtra("selected_exercises")
        selectedDay = intent.getStringExtra("selected_day")

        setupRecyclerView()

        // Kembalikan data ke DetailCustom dan simpan ke Firebase saat tombol Simpan diklik
        saveButton.setOnClickListener {
            if (exercises.isNullOrEmpty() || selectedDay.isNullOrEmpty()) {
                Toast.makeText(this, "Data tidak valid untuk disimpan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveExercisesToFirebase(selectedDay!!, exercises!!) {
                val resultIntent = Intent()
                resultIntent.putParcelableArrayListExtra("selected_exercises", exercises)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
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

    private fun saveExercisesToFirebase(day: String, updatedExercises: ArrayList<ExerciseItem>, onSuccess: () -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseRef = FirebaseDatabase.getInstance()
                .reference
                .child("user_progress")
                .child(userId)
                .child("custom_schedule")
                .child(day)
                .child("exercises")

            databaseRef.setValue(updatedExercises)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data latihan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi.", Toast.LENGTH_SHORT).show()
        }
    }
}
