package com.alight.android.aoa_launcher.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat

object TimestampUtils {
    /**
     * Timestamp to String
     * @param Timestamp
     * @return String
     */
    fun transToString(time: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)
    }

    /**
     * String to Timestamp
     * @param String
     * @return Timestamp
     */
    fun transToTimeStamp(date: String): Long {
        return SimpleDateFormat("YY-MM-DD hh-mm-ss").parse(date, ParsePosition(0)).time
    }


}
