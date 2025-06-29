package com.example.soulvent.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a given Firebase Timestamp into a human-readable date and time string.
 * @param timestamp The Firebase Timestamp object to format. Can be null.
 * @return Formatted date string (e.g., "28 Jun, 05:29 pm") or "Loading time..." if null.
 */
fun formatTimestamp(timestamp: Timestamp?): String { // <<< PARAMETER MUST BE com.google.firebase.firestore.Timestamp
    if (timestamp == null) {
        return "Loading time..." // Return a placeholder while timestamp is being set
    }

    // Convert Firebase Timestamp to java.util.Date
    val date: Date = timestamp.toDate() // This 'toDate()' method requires the above import

    // Format to "28 Jun, 05:29 pm"
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(date)
}