package com.example.esamemobile.data

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Group(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val imageUrl: String? = "",
    val description: String = "",
    val masterName: String = "",
    val masterImgUrl: String? = "",
    val masterId: String = "",
    val nextSession: String? = null
)