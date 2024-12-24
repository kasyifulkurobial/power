package com.example.powerquest.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.bumptech.glide.Glide
import android.app.Dialog
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.powerquest.R

class Statistic : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var levelTextView: TextView
    private lateinit var strengthText: TextView
    private lateinit var speedText: TextView
    private lateinit var staminaText: TextView
    private lateinit var characterImage: ImageView

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistic, container, false)

        // Bind Views
        progressBar = view.findViewById(R.id.progressBar)
        levelTextView = view.findViewById(R.id.progressTextview)
        strengthText = view.findViewById(R.id.strengthText)
        speedText = view.findViewById(R.id.speedText)
        staminaText = view.findViewById(R.id.staminaText)
        characterImage = view.findViewById(R.id.characterImage)

        setupCharacterImageClickListener()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            userRef = database.reference.child("user_progress").child(uid)
            fetchUserProgress()
        } else {
            Toast.makeText(requireContext(), "Pengguna belum login", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun setupCharacterImageClickListener() {
        characterImage.setOnClickListener {
            showGifSelectionDialog()
        }
    }

    private fun showGifSelectionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_gif_selection)

        val gifContainer: LinearLayout = dialog.findViewById(R.id.gifContainer)

        val gifs = listOf(
            Triple("Grey", R.drawable.pixel_unscreen, 0),  // Level yang dibutuhkan: 0
            Triple("Grom", R.drawable.grom, 5),           // Level yang dibutuhkan: 5
            Triple("Zera", R.drawable.zera, 10)           // Level yang dibutuhkan: 10
        )

        // Ambil level pengguna dari UI
        val currentLevel = levelTextView.text.split(":")[1].trim().toIntOrNull() ?: 0

        // Ambil GIF yang sedang dipilih
        val currentSelectedGif = when (characterImage.tag) {
            "gif_1" -> R.drawable.pixel_unscreen
            "gif_2" -> R.drawable.grom
            "gif_3" -> R.drawable.zera
            else -> null
        }

        gifs.forEach { gif ->
            val (gifName, resource, requiredLevel) = gif

            val frameLayout = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    700, // Lebar GIF
                    700  // Tinggi GIF
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
            }

            // ImageView untuk GIF
            val imageView = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(resource)
                contentDescription = "Karakter $gifName"
            }

            // ImageView untuk ikon kunci
            val lockIcon = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    200, // Lebar ikon kunci
                    200  // Tinggi ikon kunci
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
                setImageResource(R.drawable.ic_lock)
                visibility = if (currentLevel >= requiredLevel) View.GONE else View.VISIBLE
            }

            // Klik pada GIF
            frameLayout.setOnClickListener {
                when {
                    currentLevel < requiredLevel -> {
                        // Jika level tidak mencukupi
                        Toast.makeText(
                            requireContext(),
                            "Level $requiredLevel dibutuhkan untuk memilih $gifName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    currentSelectedGif == resource -> {
                        // Jika GIF yang dipilih sama dengan yang sedang digunakan
                        Toast.makeText(
                            requireContext(),
                            "Anda telah menggunakan karakter tersebut",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        // Jika level mencukupi dan GIF berbeda
                        Glide.with(this).load(resource).into(characterImage)
                        characterImage.tag = when (gifName) {
                            "Grey" -> "gif_1"
                            "Grom" -> "gif_2"
                            "Zera" -> "gif_3"
                            else -> ""
                        }
                        Toast.makeText(
                            requireContext(),
                            "$gifName selected!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Simpan karakter yang dipilih ke Firebase
                        val selectedGifTag = characterImage.tag.toString()
                        userRef.child("selectedGif").setValue(selectedGifTag)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Character saved to Firebase", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(requireContext(), "Failed to save character: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }

                        dialog.dismiss()
                    }
                }
            }

            // Tambahkan ImageView dan ikon kunci ke FrameLayout
            frameLayout.addView(imageView)
            frameLayout.addView(lockIcon)

            // Tambahkan FrameLayout ke gifContainer
            gifContainer.addView(frameLayout)
        }

        // Atur ukuran Dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% lebar layar
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun fetchUserProgress() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return // Pastikan fragment masih terpasang

                initializeProgressIfNeeded(snapshot)

                // Ambil data dari Firebase
                val currentLevel = snapshot.child("level").getValue(Int::class.java) ?: 0
                val currentProgress = snapshot.child("progress").getValue(Int::class.java) ?: 0
                val maxProgress = snapshot.child("maxProgress").getValue(Int::class.java) ?: 100
                val strength = snapshot.child("strength").getValue(Int::class.java) ?: 0
                val speed = snapshot.child("speed").getValue(Int::class.java) ?: 0
                val stamina = snapshot.child("stamina").getValue(Int::class.java) ?: 0
                val selectedGif = snapshot.child("selectedGif").getValue(String::class.java) ?: "gif_1"
                val username = snapshot.child("username").getValue(String::class.java) ?: "Username"

                // Update UI
                updateUI(currentLevel, currentProgress, maxProgress, strength, speed, stamina, selectedGif)
                unlockGifsBasedOnLevel(currentLevel)

                // Tampilkan username di tUser
                val tUser: TextView = requireView().findViewById(R.id.tUser)
                tUser.text = username
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initializeProgressIfNeeded(snapshot: DataSnapshot) {
        if (!snapshot.hasChild("level")) userRef.child("level").setValue(0)
        if (!snapshot.hasChild("progress")) userRef.child("progress").setValue(0)
        if (!snapshot.hasChild("maxProgress")) userRef.child("maxProgress").setValue(100)
        if (!snapshot.hasChild("strength")) userRef.child("strength").setValue(0)
        if (!snapshot.hasChild("speed")) userRef.child("speed").setValue(0)
        if (!snapshot.hasChild("stamina")) userRef.child("stamina").setValue(0)
        if (!snapshot.hasChild("selectedGif")) userRef.child("selectedGif").setValue("gif_1")
    }

    private fun unlockGifsBasedOnLevel(level: Int) {
        if (level >= 5) {
            userRef.child("unlockedGifs").child("gif_2").setValue("gif_2")
        }
        if (level >= 10) {
            userRef.child("unlockedGifs").child("gif_3").setValue("gif_3")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(
        level: Int,
        progress: Int,
        maxProgress: Int,
        strength: Int,
        speed: Int,
        stamina: Int,
        selectedGif: String
    ) {
        if (!isAdded) return

        // Update ProgressBar
        progressBar.max = maxProgress
        progressBar.progress = progress.coerceAtMost(maxProgress)
        levelTextView.text = "Lvl : $level"

        // Update Atribut
        strengthText.text = "💪 Strength : $strength"
        speedText.text = "⚡ Speed : $speed"
        staminaText.text = "❤️ Stamina : $stamina"

        // Update GIF
        val gifResource = when (selectedGif) {
            "gif_1" -> R.drawable.pixel_unscreen
            "gif_2" -> R.drawable.grom
            "gif_3" -> R.drawable.zera
            else -> R.drawable.pixel_unscreen
        }
        Glide.with(this).load(gifResource).into(characterImage)

        // Tetapkan warna progress bar
        updateProgressBarColor()
    }

    private fun updateProgressBarColor() {
        progressBar.progressDrawable = ContextCompat.getDrawable(requireContext(),
            R.drawable.progres_bar
        )
    }
}
