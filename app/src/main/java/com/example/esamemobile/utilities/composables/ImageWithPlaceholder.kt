package com.example.esamemobile.utilities.composables

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

//Per il momento io questa la metto, però si può forse rimuovere
enum class Size {Sm, Lg}

@Composable
fun ImageWithPlaceholder(uri: Uri?, size: Size) {
    if (false){//uri != null) {
        //Qui ci andrebbe asyncImage, ma per il momento lo ignoro
        ///Che dovrei modificare la build di gradle (serve importare il modulo coil)
    } else {
        Image(
            Icons.Outlined.Person,
            "Character picture",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier
                .size(if (size == Size.Sm) 72.dp else 128.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(if (size == Size.Sm) 20.dp else 36.dp)
            )
    }
}