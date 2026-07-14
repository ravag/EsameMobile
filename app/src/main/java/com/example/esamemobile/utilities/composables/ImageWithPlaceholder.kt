package com.example.esamemobile.utilities.composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

enum class Size {Sm, Lg}

@Composable
fun ImageWithPlaceholder(url: String?, size: Size) {
    if (!url.isNullOrBlank()){
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Immagine personaggio",
            modifier = Modifier
                .size(if (size == Size.Sm) 72.dp else 128.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        ) {
            when(painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator()
                }
                is AsyncImagePainter.State.Error -> {
                    Log.w("bug",(painter.state as AsyncImagePainter.State.Error).result.throwable)
                    Icon(Icons.Default.BrokenImage,"Errore caricamento")
                }
                else -> SubcomposeAsyncImageContent()
            }
        }
    } else {
        Image(
            Icons.Outlined.Person,
            "Immagine personaggio",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inversePrimary),
            modifier = Modifier
                .size(if (size == Size.Sm) 72.dp else 128.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ).clip(RoundedCornerShape(8.dp))
            )
    }
}