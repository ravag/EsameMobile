package com.example.esamemobile.data.firebase

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.esamemobile.R
import com.example.esamemobile.data.firebase.firestore.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MessagingService : FirebaseMessagingService(), KoinComponent {

    private val userRepository: UserRepository by inject()

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

    //Ho controllato, questi sono i metodi da usare, non so perchè dica sono deprecati che non lo sono
    //Messaggio di deprecated in java
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // il token è cambiato: va rimandato al tuo backend Java
        TokenReceiver.sendTokenToServer(token, userRepository)
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "canale_gdr"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }




}

object TokenReceiver {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //Stessa situazione di prima sempre stesso deprecated in Java
    @Suppress("DEPRECATION")
    fun newToken(userRepository: UserRepository) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                sendTokenToServer(token,userRepository)
            }
        }
    }

    fun sendTokenToServer(token: String, userRepository: UserRepository) {
        scope.launch { userRepository.updateToken(token) }
    }
}