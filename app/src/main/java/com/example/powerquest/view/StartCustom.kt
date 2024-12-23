package com.example.powerquest.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.powerquest.R
import com.example.powerquest.data.ExerciseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StartCustom : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var repsTextView: TextView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var nextButton: Button

    private var currentIndex = 0
    private var exercises: List<ExerciseItem> = emptyList()
    private var countDownTimer: CountDownTimer? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var userRef: DatabaseReference
    private val earnedProgressPerExercise = 10 // Setiap latihan Custom menyumbang 10% ke progres

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_custom)

        // Inisialisasi Views
        titleText = findViewById(R.id.titleTextCustom)
        repsTextView = findViewById(R.id.repsTextViewCustom)
        lottieAnimationView = findViewById(R.id.lottieAnimationViewCustom)
        nextButton = findViewById(R.id.nextButtonCustom)

        // Firebase: Ambil data pengguna
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            userRef = database.reference.child("user_progress").child(uid)
        } else {
            finishWithError("Pengguna belum login")
            return
        }

        // Ambil data dari Intent
        exercises = intent.getParcelableArrayListExtra("selected_exercises") ?: emptyList()

        if (exercises.isNotEmpty()) {
            showCurrentExercise()
        } else {
            titleText.text = "Tidak ada latihan"
            nextButton.text = "Kembali"
            nextButton.setOnClickListener { finish() }
        }

        nextButton.setOnClickListener {
            stopTimer()
            saveProgressToFirebase() // Update progres ke Firebase
            navigateToNextExercise()
        }
    }

    private fun navigateToNextExercise() {
        if (currentIndex < exercises.size - 1) {
            currentIndex++
            showCurrentExercise()
        } else {
            markDayAsCompleted()
        }
    }

    private fun showCurrentExercise() {
        if (currentIndex >= exercises.size) return

        val exercise = exercises[currentIndex]
        titleText.text = exercise.title
        lottieAnimationView.setAnimation(exercise.animationRes)

        when {
            exercise.reps.startsWith("x") -> {
                repsTextView.text = exercise.reps
                performReps(exercise.reps)
                nextButton.visibility = View.VISIBLE
            }
            exercise.reps.contains("Detik", ignoreCase = true) -> {
                val seconds = exercise.reps.filter { it.isDigit() }.toIntOrNull() ?: 0
                startTimer(seconds)
                nextButton.visibility = View.VISIBLE
            }
            else -> {
                repsTextView.text = "Format Tidak Valid"
                nextButton.visibility = View.VISIBLE
            }
        }
    }

    private fun startTimer(seconds: Int) {
        if (seconds <= 0) {
            repsTextView.text = "00:00"
            navigateToNextExercise()
            return
        }

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                repsTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                repsTextView.text = "00:00"
                navigateToNextExercise()
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    @SuppressLint("SetTextI18n")
    private fun performReps(reps: String) {
        val totalReps = reps.removePrefix("x").toIntOrNull() ?: 1
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
        repsTextView.text = "x$totalReps"
    }

    private fun saveProgressToFirebase() {
        userRef.get().addOnSuccessListener { snapshot ->
            val currentProgress = snapshot.child("progress").getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 0
            val currentStrength = snapshot.child("strength").getValue(Int::class.java) ?: 0
            val currentSpeed = snapshot.child("speed").getValue(Int::class.java) ?: 0
            val currentStamina = snapshot.child("stamina").getValue(Int::class.java) ?: 0

            val newProgress = currentProgress + earnedProgressPerExercise
            if (newProgress >= 100) {
                // Level up logic
                userRef.child("level").setValue(currentLevel + 1)
                userRef.child("progress").setValue(newProgress - 100)
                userRef.child("strength").setValue(currentStrength + 3)
                userRef.child("speed").setValue(currentSpeed + 2)
                userRef.child("stamina").setValue(currentStamina + 4)
            } else {
                userRef.child("progress").setValue(newProgress)
            }
        }
    }

    private fun markDayAsCompleted() {
        val selectedDay = intent.getStringExtra("selected_day")
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null && !selectedDay.isNullOrEmpty()) {
            val uid = currentUser.uid
            FirebaseDatabase.getInstance().reference
                .child("user_progress")
                .child(uid)
                .child("custom_schedule")
                .child(selectedDay)
                .child("status")
                .setValue("completed")
                .addOnSuccessListener {
                    Toast.makeText(this, "Hari $selectedDay selesai!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    android.util.Log.e("StartCustom", "Gagal mengupdate status: $error")
                    Toast.makeText(this, "Gagal memperbarui status untuk $selectedDay.", Toast.LENGTH_SHORT).show()
                }
        }

        // Kembali ke CustomFragment
        val resultIntent = Intent().apply {
            putExtra("selected_day", selectedDay)
            putExtra("day_completed", true)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    private fun finishWithError(message: String) {
        android.util.Log.e("StartCustom", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
