package com.example.pigeonsandseeds.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FirebaseFirestore

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val triggeringGeofences = geofencingEvent?.triggeringGeofences ?: return
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            for (geofence in triggeringGeofences) {
                val businessId = geofence.requestId
                showBusinessNotification(context, businessId)
            }
        }
    }

    private fun showBusinessNotification(context: Context, businessId: String) {
        db.collection("businesses").document(businessId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val businessName = document.getString("name") ?: return@addOnSuccessListener
                    val currentOffer = document.getString("currentOffer") ?: return@addOnSuccessListener

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    val channelId = "business_notifications"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = android.app.NotificationChannel(
                            channelId,
                            "Business Notifications",
                            android.app.NotificationManager.IMPORTANCE_DEFAULT
                        )
                        notificationManager.createNotificationChannel(channel)
                    }

                    val notification = android.app.NotificationCompat.Builder(context, channelId)
                        .setContentTitle(businessName)
                        .setContentText(currentOffer)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(businessId.hashCode(), notification)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching business data: ${e.message}")
            }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
} 