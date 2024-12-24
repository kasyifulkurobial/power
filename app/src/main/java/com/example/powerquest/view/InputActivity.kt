package com.example.powerquest.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.powerquest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InputActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var saveButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // Inisialisasi Firebase Auth dan Database
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish() // Tutup Activity jika user tidak login
            return
        }

        // Inisialisasi Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("user_progress")

        // View Binding
        usernameInput = findViewById(R.id.username)
        saveButton = findViewById(R.id.btn_save)

        saveButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUsernameToFirebase(currentUser.uid, username)
        }
    }

    private fun saveUsernameToFirebase(uid: String, username: String) {
        // Buat data yang akan disimpan
        val userData = mapOf(
            "username" to username
        )

        // Simpan ke Firebase Realtime Database
        databaseReference.child(uid).setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Username saved successfully", Toast.LENGTH_SHORT).show()

                // Arahkan ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Tutup InputActivity
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save username: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("InputActivity", "Error saving username", exception)
            }
    }
}
