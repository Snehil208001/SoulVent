package com.example.soulvent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.soulvent.nav.Screen
import com.example.soulvent.nav.SoulMateNavGraph
import com.example.soulvent.ui.theme.SoulVentTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.SetOptions

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.d(TAG, "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoulVentTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    intent?.getStringExtra("postId")?.let { postId ->
                        Log.d(TAG, "Deep linking to postId: $postId")
                        navController.navigate(Screen.Comments.createRoute(postId)) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SoulMateNavGraph(navController = navController, paddingValues = innerPadding)
                }
            }
        }

        setupAnonymousAuthAndFCMToken()
        askNotificationPermission()
    }

    private fun setupAnonymousAuthAndFCMToken() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInAnonymously:success")
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                if (!tokenTask.isSuccessful) {
                                    Log.w(TAG, "Fetching FCM registration token failed", tokenTask.exception)
                                    return@addOnCompleteListener
                                }
                                val token = tokenTask.result
                                if (token != null) {
                                    Log.d(TAG, "Retrieved FCM token after anonymous auth: $token")
                                    firestore.collection("users")
                                        .document(userId)
                                        .set(
                                            mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
                                            SetOptions.merge()
                                        )
                                        .addOnSuccessListener {
                                            Log.d(TAG, "FCM token saved successfully for anonymous user: $userId")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error saving FCM token for anonymous user: $userId", e)
                                        }
                                }
                            }
                        } else {
                            Log.e(TAG, "Anonymous sign-in success but userId is null.")
                        }
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                    }
                }
        } else {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                Log.d(TAG, "User already signed in (UID: $userId). Ensuring FCM token is saved.")
                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                    if (!tokenTask.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", tokenTask.exception)
                        return@addOnCompleteListener
                    }
                    val token = tokenTask.result
                    if (token != null) {
                        firestore.collection("users")
                            .document(userId)
                            .set(
                                mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
                                SetOptions.merge()
                            )
                            .addOnSuccessListener {
                                Log.d(TAG, "FCM token ensured for user: $userId")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error ensuring FCM token for user: $userId", e)
                            }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
