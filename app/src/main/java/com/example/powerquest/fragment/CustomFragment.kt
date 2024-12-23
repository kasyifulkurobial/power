package com.example.powerquest.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.powerquest.view.DetailCustom
import com.example.powerquest.R
import com.example.powerquest.adapter.DaysAdapter
import com.example.powerquest.adapter.ExercisesAdapter
import com.example.powerquest.data.ExerciseItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomFragment : Fragment() {

    private lateinit var buttonAdd: Button
    private lateinit var recyclerViewDays: RecyclerView
    private lateinit var textDefault: TextView
    private lateinit var database: DatabaseReference
    private lateinit var userUID: String

    private val selectedExercisesByDay = mutableMapOf<String, MutableList<ExerciseItem>>()
    private val daysList = mutableListOf<String>()

    companion object {
        private const val REQUEST_CODE_DETAIL_CUSTOM = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom, container, false)

        // Inisialisasi View
        buttonAdd = view.findViewById(R.id.button_add)
        recyclerViewDays = view.findViewById(R.id.recycler_view_days)
        textDefault = view.findViewById(R.id.text_default)

        recyclerViewDays.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewDays.adapter = DaysAdapter(daysList) { selectedDay ->
            val exercisesForDay = selectedExercisesByDay[selectedDay] ?: mutableListOf()

            val intent = Intent(requireContext(), DetailCustom::class.java)
            intent.putParcelableArrayListExtra("selected_exercises", ArrayList(exercisesForDay))
            intent.putExtra("selected_day", selectedDay)
            startActivityForResult(intent, REQUEST_CODE_DETAIL_CUSTOM)
        }

        // Mendapatkan UID pengguna yang sedang login
        userUID = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        database = FirebaseDatabase.getInstance().reference.child("user_progress").child(userUID).child("custom_schedule")

        // Baca data dari Firebase
        readDataFromFirebase()

        // Aktifkan fitur geser untuk menghapus
        enableSwipeToDelete()

        buttonAdd.setOnClickListener {
            showAddExerciseBottomSheet()
        }

        return view
    }

    private fun readDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                selectedExercisesByDay.clear()
                daysList.clear()

                for (daySnapshot in snapshot.children) {
                    val day = daySnapshot.key ?: continue
                    val exercises = daySnapshot.child("exercises").children.mapNotNull {
                        it.getValue(ExerciseItem::class.java)
                    }.toMutableList()

                    selectedExercisesByDay[day] = exercises
                    daysList.add(day)
                }

                daysList.sort()
                recyclerViewDays.adapter?.notifyDataSetChanged()
                updateUIVisibility()

                // Periksa dan reset jika semua hari selesai
                checkAndResetCompletion()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUIVisibility() {
        if (daysList.isEmpty()) {
            recyclerViewDays.visibility = View.GONE
            textDefault.visibility = View.VISIBLE
        } else {
            recyclerViewDays.visibility = View.VISIBLE
            textDefault.visibility = View.GONE
        }
    }

    private fun enableSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position in daysList.indices) {
                    val dayToDelete = daysList[position]
                    showDeleteConfirmationDialog(position, dayToDelete)
                } else {
                    recyclerViewDays.adapter?.notifyItemChanged(position)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewDays)
    }

    private fun showDeleteConfirmationDialog(position: Int, dayToDelete: String) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus $dayToDelete?")
            .setPositiveButton("Oke") { _, _ ->
                database.child(dayToDelete).removeValue().addOnSuccessListener {
                    if (position in daysList.indices) {
                        daysList.removeAt(position)
                        recyclerViewDays.adapter?.notifyItemRemoved(position)
                        updateUIVisibility()
                    }
                }.addOnFailureListener {
                    recyclerViewDays.adapter?.notifyItemChanged(position)
                }
            }
            .setNegativeButton("Batal") { _, _ ->
                recyclerViewDays.adapter?.notifyItemChanged(position)
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    private fun checkAndResetCompletion() {
        if (daysList.isEmpty()) {
            // Jika daysList kosong, tidak perlu melakukan pengecekan
            return
        }

        database.get().addOnSuccessListener { snapshot ->
            val allCompleted = daysList.all { day ->
                val status = snapshot.child(day).child("status").getValue(String::class.java)
                status == "completed"
            }

            if (allCompleted) {
                val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Selamat!")
                    .setMessage("Anda Telah Menyelesaikan Semua Exercise. Semua Exercise akan di restart kembali.")
                    .setPositiveButton("OK") { _, _ ->
                        for (day in daysList) {
                            database.child(day).child("status").setValue("not_completed")
                                .addOnSuccessListener {
                                    Log.d("CustomFragment", "$day status updated to not_completed")
                                }
                                .addOnFailureListener {
                                    Log.e("CustomFragment", "Failed to update status for $day", it)
                                }
                        }
                        readDataFromFirebase() // Refresh data di UI
                    }
                    .setCancelable(false)
                    .create()

                alertDialog.show()
            }
        }.addOnFailureListener {
            Log.e("CustomFragment", "Failed to fetch data from Firebase", it)
        }
    }


    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun showAddExerciseBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        bottomSheetDialog.setContentView(view)

        val recyclerViewExercises = view.findViewById<RecyclerView>(R.id.recycler_view_exercises)
        val buttonAddExercise = view.findViewById<Button>(R.id.button_add_exercise)
        val spinnerDay = view.findViewById<Spinner>(R.id.spinner_day)

        val daysArray = resources.getStringArray(R.array.days_array)
        val spinnerAdapter = ArrayAdapter(
            requireContext(), R.layout.spinner_item, daysArray
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerDay.adapter = spinnerAdapter

        val exercises = listOf(
            ExerciseItem("Box Jump", R.raw.box_jump, "x15"),
            ExerciseItem("Bumper", R.raw.bumper, "x10"),
            ExerciseItem("Burpees", R.raw.burpees, "x15"),
            ExerciseItem("Chair Stand", R.raw.chair_stand, "x10"),
            ExerciseItem("Cobra", R.raw.cobras, "x15"),
            ExerciseItem("Frog Press", R.raw.frog_press, "x10"),
            ExerciseItem("High Knee", R.raw.high_knees, "x15"),
            ExerciseItem("Inchworm", R.raw.inchworm, "x10"),
            ExerciseItem("Jumping Jack", R.raw.jumping_jack, "x15"),
            ExerciseItem("Jumping Squats", R.raw.jumping_squats, "x10"),
            ExerciseItem("Leg Up", R.raw.leg_up, "x15"),
            ExerciseItem("Press Up", R.raw.press_up, "x10"),
            ExerciseItem("Pull Up", R.raw.pull_up, "x15"),
            ExerciseItem("Punches", R.raw.punches, "x10"),
            ExerciseItem("Push Up", R.raw.push_up, "x15"),
            ExerciseItem("Reverse Crunches", R.raw.reverse_crunches, "x10"),
            ExerciseItem("Rope", R.raw.rope, "x15"),
            ExerciseItem("Sprint", R.raw.run, "15 Detik"),
            ExerciseItem("Single Leg Hip", R.raw.single_leg_hip, "30 Detik"),
            ExerciseItem("Sit Up", R.raw.sit_up, "10"),
            ExerciseItem("Split Jump", R.raw.split_jump, "x15"),
            ExerciseItem("Squat Kick", R.raw.squat_kicks, "x10"),
            ExerciseItem("Squat Reach", R.raw.squat_reach, "x15")
        )

        val selectedExercises = mutableListOf<ExerciseItem>()
        val exerciseAdapter = ExercisesAdapter(exercises) { selectedItem ->
            if (!selectedExercises.contains(selectedItem)) {
                selectedExercises.add(selectedItem)
            }
        }

        recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewExercises.adapter = exerciseAdapter

        buttonAddExercise.setOnClickListener {
            val selectedDay = spinnerDay.selectedItem.toString()
            val exercisesForDay = selectedExercisesByDay.getOrPut(selectedDay) { mutableListOf() }
            exercisesForDay.addAll(selectedExercises)

            database.child(selectedDay).child("exercises").setValue(exercisesForDay)

            if (!daysList.contains(selectedDay)) {
                daysList.add(selectedDay)
                daysList.sort()
                recyclerViewDays.adapter?.notifyDataSetChanged()
            }

            updateUIVisibility()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}