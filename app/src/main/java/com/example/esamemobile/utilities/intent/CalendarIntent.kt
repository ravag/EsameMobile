package com.example.esamemobile.utilities.intent

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast

fun addEventToCalendar(
    context: Context,
    title: String,
    startTime: Long,
    duration: Int = 180
) {
    //metto un evento di tre ore, tre ore sono giusto indicative, non penso nessuno riesca a dire quanto duri una sessione
    val endTime = startTime + duration*60*1000

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
        putExtra(CalendarContract.Events.TITLE, title)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context,"Impossibile aggiungere al calendario", Toast.LENGTH_SHORT).show()
    }

}