package com.example.powerquest.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseItem(
    var title: String = "",
    var animationRes: Int = 0,
    var reps: String = ""
) : Parcelable
