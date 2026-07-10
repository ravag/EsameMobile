package com.example.esamemobile.data.supabase

import android.util.Log
import io.github.jan.supabase.storage.Storage

interface ImagesRepository {
    suspend fun uploadImage(fileBytes: ByteArray, fileName: String, path: String): String
}

class ImagesRepositoryImpl(private val storage: Storage): ImagesRepository {

    override suspend fun uploadImage(fileBytes: ByteArray, fileName: String, path: String): String {
        val bucket = storage.from("images")

        bucket.upload("$path/${fileName}.jpg",fileBytes) {
            upsert = false
        }

        return   bucket.publicUrl("$path/${fileName}.jpg")
    }

}
