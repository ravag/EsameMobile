package com.example.esamemobile.utilities.intent

import android.content.Context
import android.content.Intent

fun sendMessageIntent(context: Context, message: String,title: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT,message)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(sendIntent, title))
}