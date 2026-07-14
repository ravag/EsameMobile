package com.example.esamemobile.data.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.esamemobile.MainActivity
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

        val data = message.data
        if (data.isNotEmpty()) {
            val groupId = data["groupId"]
            val title = data["title"]
            val body = data["body"]

            showNotification(title,body, groupId)
        }
    }

    //onNewToken è deprecato in java, la documentazione dice di usare questo metodo, io sopprimo il warning
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        TokenReceiver.sendTokenToServer(token, userRepository)
    }

    private fun showNotification(title: String?, body: String?, groupId: String?) {
        val channelId = "canale_gdr"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("groupId",groupId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
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