package com.example.powerquest.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.view.DetailActivity
import com.example.powerquest.R
import com.example.powerquest.adapter.AdapterClass
import com.google.firebase.database.*

class DailyFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: AdapterClass
    private lateinit var database: DatabaseReference

    // List untuk menampung data dari Firebase
    private val imageList = mutableListOf<Int>()
    private val titleList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daily, container, false)

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("categories")

        // Baca data dari Firebase
        readCategoriesFromFirebase()

        return view
    }

    private fun readCategoriesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageList.clear()
                titleList.clear()

                for (categorySnapshot in snapshot.children) {
                    val title = categorySnapshot.key
                    val imageName = categorySnapshot.child("image").value as? String

                    if (title != null && imageName != null) {
                        titleList.add(title)

                        // Ambil resource ID berdasarkan nama resource
                        val imageResId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
                        imageList.add(imageResId)
                    }
                }

                if (titleList.isNotEmpty() && imageList.isNotEmpty()) {
                    myAdapter = AdapterClass(imageList.toTypedArray(), titleList.toTypedArray()) { position ->
                        navigateToDetailActivity(titleList[position])
                    }
                    recyclerView.adapter = myAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal membaca data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun navigateToDetailActivity(title: String) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("title", title)
        }
        startActivity(intent)
    }
}
