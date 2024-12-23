package com.example.powerquest.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.powerquest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading)

        supportActionBar?.hide()

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            val intent = Intent(this@LoadingApp, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}