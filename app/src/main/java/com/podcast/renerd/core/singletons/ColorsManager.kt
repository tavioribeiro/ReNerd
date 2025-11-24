package com.podcast.renerd.core.singletons

import androidx.core.content.ContextCompat
import com.podcast.renerd.R








object ColorsManager {
    val colorsMap = mapOf(
        0 to R.color.color0,
        1 to R.color.color1,
        2 to R.color.color2,
        3 to R.color.color3,
        4 to R.color.color4,
        5 to R.color.color5,
        6 to R.color.color6,
        7 to R.color.color7,
        8 to R.color.color8,
        9 to R.color.color9,
    )

    fun getColorHex(from: Int): String {
        val colorInt = ContextCompat.getColor(ContextManager.getContext(), colorsMap[from]!!)
        return String.format("#%08X", colorInt)
    }
}