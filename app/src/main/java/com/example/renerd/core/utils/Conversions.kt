package com.example.renerd.core.utils



fun formatTime(milliseconds: Int): String {
    val minutes = milliseconds / 1000 / 60
    val seconds = (milliseconds / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


fun calculatePercentage(part: Double, whole: Double): Int {
    return ((part / whole) * 100).toInt()
}

fun calculateOriginalValue(percentage: Double, part: Double): Int {
    return ((part * 100) / percentage).toInt()
}

fun calculatePartFromPercentage(percentage: Double, whole: Double): Int {
    return ((percentage / 100) * whole).toInt()
}