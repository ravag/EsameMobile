package com.example.esamemobile.data.firebase

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Se hai usato .setNotification() nel backend, arriva qui:
        message.notification?.let {
            showNotification(it.title, it.body)
        }

        // Se hai usato .putData() nel backend, arriva qui come mappa:
        val data = message.data
        if (data.isNotEmpty()) {
            val docId = data["docId"]
            val changeType = data["changeType"]
            // gestisci la logica in base ai dati, es. refresh di una lista
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // il token è cambiato: va rimandato al tuo backend Java
        TokenReceiver.sendTokenToServer(token)
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "default_channel"

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }




}

object TokenReceiver {
    fun newToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                sendTokenToServer(token)
            }
        }
    }

    fun sendTokenToServer(token: String) {
        DatabaseServices.updateToken(token)
    }
}