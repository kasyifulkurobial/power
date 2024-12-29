package com.example.powerquest.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseItem(
    var title: String = "",
    var animationRes: String = "",
    var reps: String = ""
) : Parcelable
