package com.example.powerquest.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.powerquest.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        firebaseAuth = FirebaseAuth.getInstance()

        // Referensi view
        val emailTextView: TextView = findViewById(R.id.emailTextView)
        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)
        val deleteAccountButton: Button = findViewById(R.id.deleteAccountButton)

        // Tampilkan email pengguna saat ini
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            emailTextView.text = currentUser.email
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }

        // Logika untuk mengubah password
        changePasswordButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
            val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.oldPasswordEditText)
            val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
            val changePasswordButton = dialogView.findViewById<Button>(R.id.changePasswordButton)

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .create()

            // Set up button click listener
            changePasswordButton.setOnClickListener {
                val oldPassword = oldPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()

                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    val email = currentUser?.email
                    if (email != null) {
                        val credential = EmailAuthProvider.getCredential(email, oldPassword)
                        currentUser.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                            if (reAuthTask.isSuccessful) {
                                // Re-authentication success, update password
                                currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(this, "Password successfully changed", Toast.LENGTH_SHORT).show()
                                        alertDialog.dismiss() // Close the dialog
                                    } else {
                                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Incorrect old password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            alertDialog.show()
        }


        // Logika untuk menghapus akun
        deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    currentUser?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
