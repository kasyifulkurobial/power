package com.example.powerquest.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.powerquest.adapter.AnimationAdapter
import com.example.powerquest.databinding.ActivityDetailBinding
import com.google.firebase.database.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var animationAdapter: AnimationAdapter
    private lateinit var database: DatabaseReference

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data kategori dari Intent
        val title = intent.getStringExtra("title") ?: run {
            Toast.makeText(this, "Kategori tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("categories").child(title)

        // Baca data kategori
        readCategoryDataFromFirebase()
    }

    private fun readCategoryDataFromFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("DiscouragedApi")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@DetailActivity, "Kategori tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                val imageName = snapshot.child("image").value as? String ?: ""
                val imageResId = resources.getIdentifier(imageName, "drawable", packageName)

                // Ambil data latihan
                val exercises = snapshot.child("exercises").children.mapNotNull {
                    val animation = it.child("animation").value as? String
                    val name = it.child("name").value as? String
                    val reps = it.child("reps").value as? String
                    if (animation != null && name != null && reps != null) {
                        Triple(animation, name, reps)
                    } else {
                        null
                    }
                }

                val animations = exercises.map {
                    resources.getIdentifier(it.first, "raw", packageName)
                }
                val names = exercises.map { it.second }
                val reps = exercises.map { it.third }

                // Update UI
                setupUI(snapshot.key.orEmpty(), imageResId, animations, names, reps.toMutableList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Gagal membaca data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(
        title: String,
        imageResId: Int,
        animations: List<Int>,
        names: List<String>,
        reps: MutableList<String>
    ) {
        // Set gambar header
        if (imageResId != -1) {
            binding.headerImage.setImageResource(imageResId)
        }

        // Set title dan subtitle
        binding.detailTitle.text = title
        binding.detailSubtitle.text = "${animations.size} Latihan"

        // Setup RecyclerView
        animationAdapter = AnimationAdapter(animations, names, reps)
        binding.exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.exerciseRecyclerView.adapter = animationAdapter

        // Tombol Mulai
        binding.startButton.setOnClickListener {
            if (animations.isNotEmpty() && names.isNotEmpty() && reps.isNotEmpty()) {
                val intent = Intent(this, StartActivity::class.java).apply {
                    putExtra("animations", animations.toIntArray())
                    putExtra("names", names.toTypedArray())
                    putExtra("reps", reps.toTypedArray())
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@DetailActivity,
                    "Data tidak lengkap untuk memulai latihan!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
