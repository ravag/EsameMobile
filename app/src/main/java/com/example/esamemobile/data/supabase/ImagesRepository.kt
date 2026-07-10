package com.example.esamemobile.data.supabase

import android.util.Log
import io.github.jan.supabase.storage.Storage

interface ImagesRepository {
    suspend fun uploadImage(fileBytes: ByteArray, fileName: String, path: String): Result<String>
}

class ImagesRepositoryImpl(private val storage: Storage): ImagesRepository {

    override suspend fun uploadImage(fileBytes: ByteArray, fileName: String, path: String): Result<String> {
        return try {
            val bucket = storage.from("images")
            val filePath = "$path/${fileName}.jpg"

            bucket.upload(filePath,fileBytes) {
                upsert = true
            }

            val url = "${bucket.publicUrl(filePath)}?t=${System.currentTimeMillis()}"
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

}
