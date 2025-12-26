package com.example.yourway

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class YourWayFirebaseMessagingService : FirebaseMessagingService() {
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // Handle incoming notification
//        val postId: String = remoteMessage.getData().get("post_id").toString()
//        // Display the notification to the user
//        displayNotification(postId)
//    }
//
//    override fun onNewToken(token: String) {
//        // Send the new FCM token to your server
//        sendTokenToServer(token)
//    }
//    private fun sendTokenToServer(token: String) {
//        // Send the FCM token to your server using an HTTP request or another method
//        // This will allow your server to send notifications to the device using the FCM token
//    }
//    private fun displayNotification(postId: String) {
//        // Create a notification builder
//        val notificationBuilder = NotificationCompat.Builder(this)
//        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
//        notificationBuilder.setLargeIcon(
//            BitmapFactory.decodeResource(
//                getResources(),
//                R.drawable.ic_launcher
//            )
//        )
//        notificationBuilder.setContentTitle("New Post")
//        notificationBuilder.setContentText("Check out the new post!")
//        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        // Create an intent to open the post
//        val intent: Intent = Intent(
//            this,
//            PostActivity::class.java
//        )
//        intent.putExtra("post_id", postId)
//
//        // Create a pending intent
//        val pendingIntent =
//            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        // Set the pending intent on the notification builder
//        notificationBuilder.setContentIntent(pendingIntent)
//
//        // Build and display the notification
//        val notificationManager = NotificationManagerCompat.from(this)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // Request the permission
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                    12345
//                )
//                return
//            }
//        }
//
//        notificationManager.notify(12345, notificationBuilder.build())
//    }
}