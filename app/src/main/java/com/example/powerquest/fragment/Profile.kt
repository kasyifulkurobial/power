package com.example.powerquest.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.powerquest.view.Login
import com.example.powerquest.data.ProfileItem
import com.example.powerquest.R
import com.example.powerquest.adapter.ProfileAdapter
import com.example.powerquest.databinding.FragmentProfileBinding
import com.example.powerquest.view.AboutActivity
import com.example.powerquest.view.AccountActivity
import com.google.firebase.auth.FirebaseAuth

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        // RecyclerView setup
        val profileItems = listOf(
            ProfileItem(R.drawable.ic_unlocked, "Account"),
            ProfileItem(R.drawable.ic_info, "About"),
            ProfileItem(R.drawable.ic_exit, "Log Out")
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ProfileAdapter(profileItems) { profileItem ->
            handleItemClick(profileItem)
        }

        return binding.root
    }

    private fun handleItemClick(profileItem: ProfileItem) {
        when (profileItem.title) {
            "Account" -> {
                // Navigate to AccountActivity
                val intent = Intent(requireContext(), AccountActivity::class.java)
                startActivity(intent)
            }
            "About" -> {
                // Handle about menu
                val intent = Intent(requireContext(), AboutActivity::class.java)
                startActivity(intent)
            }
            "Log Out" -> {
                // Log out and navigate to login screen
                firebaseAuth.signOut()
                val intent = Intent(requireContext(), Login::class.java)
                startActivity(intent)
                activity?.finish()
            }
            else -> {
                Toast.makeText(requireContext(), "Unknown option", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
