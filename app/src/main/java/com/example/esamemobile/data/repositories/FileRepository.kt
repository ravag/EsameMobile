package com.example.esamemobile.data.repositories

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FileRepository {
    suspend fun readBytes(uri: Uri): ByteArray?
}

class FileRepositoryImpl(private val context: Context): FileRepository {

    override suspend fun readBytes(uri: Uri): ByteArray? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }
}