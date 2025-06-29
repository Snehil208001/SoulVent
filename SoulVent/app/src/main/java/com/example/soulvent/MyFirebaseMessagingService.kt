package com.example.soulvent // <--- IMPORTANT: Make sure this package matches your app's base package

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth // Needed to get the current user ID
import com.google.firebase.firestore.FieldValue // Needed for arrayUnion
import com.google.firebase.firestore.FirebaseFirestore // Needed to save tokens
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.soulvent.MainActivity // Import your MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"


    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {

            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("fcmTokens", FieldValue.arrayUnion(token))
                .addOnSuccessListener { Log.d(TAG, "FCM token saved successfully for user: $userId") }
                .addOnFailureListener { e -> Log.e(TAG, "Error saving FCM token for user: $userId", e) }
        } else {
            Log.w(TAG, "User not logged in, cannot save FCM token.")
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")


        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            val type = remoteMessage.data["type"] // e.g., "new_comment", "new_like"
            val title = remoteMessage.data["title"] ?: "New SoulVent Activity" // Default title if not provided
            val body = remoteMessage.data["body"] ?: "Check out what's new!" // Default body
            val postId = remoteMessage.data["postId"] // ID of the post, useful for deep linking

            sendNotification(title, body, postId)
        }


        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Title: ${notification.title}")
            Log.d(TAG, "Message Notification Body: ${notification.body}")

            val title = notification.title ?: "New SoulVent Update"
            val body = notification.body ?: "Something new happened!"
            val postId = remoteMessage.data["postId"]

            sendNotification(title, body, postId)
        }
    }

    private fun sendNotification(title: String, body: String, postId: String?) {

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            postId?.let {
                putExtra("postId", it)

            }
        }


        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code, unique for different notifications */,
            intent,
            pendingIntentFlags
        )


        val channelId = "soulvent_updates_channel" // Unique ID for your channel
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SoulVent Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new comments and likes on your vents."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")

                return
            }
        }

        notificationManager.notify(0 /* Unique ID for this notification */, notificationBuilder.build())
    }
}