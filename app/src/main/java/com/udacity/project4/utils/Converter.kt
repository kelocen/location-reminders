package com.udacity.project4.utils

import androidx.databinding.InverseMethod

/**
 * A utility object for converting [Double] to [String] for the **latitude and longitude** of
 * each reminder.
 */
object Converter {
    @InverseMethod("stringToDouble")
    @JvmStatic
            /**
             * Converts the latitude and longitude from a double to a string.
             * @param coordinate A [Double] that contains latitude or longitude.
             * @return A [String] with the latitude or longitude
             */
    fun doubleToString(coordinate: Double): String {
        return coordinate.toString()
    }

    @JvmStatic
            /**
             * Converts latitude from a string to a double.
             * @param coordinate A [String] that contains latitude or longitude
             * @return A [Double] with the latitude or longitude.
             */
    fun stringToDouble(coordinate: String): Double {
        if (coordinate.isEmpty()) {
            return 0.0
        }
        return coordinate.toDouble()
    }
}