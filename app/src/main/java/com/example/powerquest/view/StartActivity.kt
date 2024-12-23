package com.example.powerquest.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.powerquest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StartActivity : AppCompatActivity() {

    private lateinit var animationView: LottieAnimationView
    private lateinit var titleText: TextView
    private lateinit var repsTextView: TextView
    private lateinit var nextButton: Button

    private var animationIndex = 0
    private lateinit var animations: List<Int>
    private lateinit var names: List<String>
    private lateinit var reps: List<String>
    private var activeTimer: CountDownTimer? = null
    private val earnedProgressPerExercise = 5 // Contoh: Setiap latihan menyumbang 20% ke progres

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // Bind views
        animationView = findViewById(R.id.lottieAnimationView)
        titleText = findViewById(R.id.titleText)
        repsTextView = findViewById(R.id.repsTextView)
        nextButton = findViewById(R.id.nextButton)

        // Firebase reference
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            userRef = database.reference.child("user_progress").child(uid)
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Get data from intent
        animations = intent.getIntArrayExtra("animations")?.toList() ?: emptyList()
        names = intent.getStringArrayExtra("names")?.toList() ?: emptyList()
        reps = intent.getStringArrayExtra("reps")?.toList() ?: emptyList()

        // Validate if data is available
        if (animations.isEmpty() || names.isEmpty() || reps.isEmpty()) {
            finishWithError("Data tidak ditemukan! Pastikan semua data dikirim dengan benar.")
            return
        }

        // Initialize first animation
        showAnimation(animationIndex)

        // Set Next Button click listener
        nextButton.setOnClickListener {
            activeTimer?.cancel() // Cancel any active timer before moving to the next animation

            saveProgressToFirebase() // Update progress after completing current exercise

            if (animationIndex < animations.size - 1) {
                animationIndex++
                showAnimation(animationIndex)
            } else {
                Toast.makeText(this, "Semua latihan selesai!", Toast.LENGTH_LONG).show()
                finish() // End activity when all animations are shown
            }
        }
    }

    private fun showAnimation(index: Int) {
        if (index < 0 || index >= animations.size) {
            finishWithError("Indeks animasi tidak valid! Indeks: $index, Ukuran list: ${animations.size}")
            return
        }

        // Update UI for the current animation
        animationView.setAnimation(animations[index])
        titleText.text = names[index]
        animationView.playAnimation()

        val currentReps = reps[index]
        if (currentReps.contains("Detik")) {
            val duration = currentReps.replace(" Detik", "").toInt() * 1000L
            startCountdown(duration)
        } else if (currentReps.startsWith("x")) {
            val repetitions = currentReps.replace("x", "").toInt()
            showRepetitionInstruction(repetitions)
        }
    }

    private fun startCountdown(duration: Long) {
        repsTextView.visibility = View.VISIBLE
        activeTimer?.cancel()

        activeTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                repsTextView.text = "${millisUntilFinished / 1000} Detik"
            }

            override fun onFinish() {
                repsTextView.text = "Selesai"
            }
        }.start()
    }

    private fun showRepetitionInstruction(repetitions: Int) {
        repsTextView.visibility = View.VISIBLE
        activeTimer?.cancel()
        repsTextView.text = "$repetitions kali"
    }

    private fun saveProgressToFirebase() {
        userRef.get().addOnSuccessListener { snapshot ->
            val currentProgress = snapshot.child("progress").getValue(Int::class.java) ?: 0
            val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 0
            val currentMaxProgress = snapshot.child("maxProgress").getValue(Int::class.java) ?: 100
            val currentStrength = snapshot.child("strength").getValue(Int::class.java) ?: 0
            val currentSpeed = snapshot.child("speed").getValue(Int::class.java) ?: 0
            val currentStamina = snapshot.child("stamina").getValue(Int::class.java) ?: 0

            val newProgress = currentProgress + earnedProgressPerExercise
            if (newProgress >= currentMaxProgress) {
                // Level up logic
                userRef.child("level").setValue(currentLevel + 1)
                userRef.child("progress").setValue(newProgress - currentMaxProgress)

                // Tingkatkan maxProgress sebesar 10%
                val newMaxProgress = (currentMaxProgress * 1.1).toInt()
                userRef.child("maxProgress").setValue(newMaxProgress)

                // Tingkatkan atribut
                userRef.child("strength").setValue(currentStrength + 3) // Strength bertambah 3
                userRef.child("speed").setValue(currentSpeed + 2)       // Speed bertambah 2
                userRef.child("stamina").setValue(currentStamina + 4)   // Stamina bertambah 4
            } else {
                userRef.child("progress").setValue(newProgress)
            }
        }
    }

    private fun finishWithError(message: String) {
        android.util.Log.e("StartActivity", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
