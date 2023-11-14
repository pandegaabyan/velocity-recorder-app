package com.example.velocity_recorder.utils

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date

object ClockUtils {

    fun getTime(duration: Long): String {
        return DateUtils.formatElapsedTime(duration)
    }

    fun getRelativeTime(timeInMillis: Long): String {
        val relativeTimeSpanString: CharSequence? = DateUtils.getRelativeTimeSpanString(
            timeInMillis,
            System.currentTimeMillis(),
            DateUtils.FORMAT_SHOW_TIME.toLong()
        )
        return relativeTimeSpanString?.toString() ?: ""
    }

    @SuppressLint("SimpleDateFormat")
    fun convertLongToString(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return format.format(date)
    }
}