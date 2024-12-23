package com.example.powerquest.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.powerquest.fragment.Profile
import com.example.powerquest.fragment.Quest
import com.example.powerquest.R
import com.example.powerquest.fragment.Statistic
import com.example.powerquest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Tampilkan fragment Quest secara default
        replaceFragment(Quest())

        // Item "Quest" aktif di BottomNavigationView
        binding.bottomNavigationView.selectedItemId = R.id.quest

        // Atur padding untuk system bars (jika diperlukan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigasi antar-fragment
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.quest -> replaceFragment(Quest())
                R.id.profile -> replaceFragment(Profile())
                R.id.statistic -> replaceFragment(Statistic())
                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
