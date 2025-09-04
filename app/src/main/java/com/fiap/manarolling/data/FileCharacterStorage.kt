package com.fiap.manarolling.data

import android.content.Context
import com.fiap.manarolling.model.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FileCharacterStorage(private val context: Context) {
    private val lock = ReentrantLock()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file: File by lazy { File(context.filesDir, "characters.json") }

    @Serializable
    private data class Wrapper(val items: List<Character>)

    suspend fun readAll(): List<Character> = withContext(Dispatchers.IO) {
        lock.withLock {
            if (!file.exists()) emptyList()
            else {
                val text = file.readText()
                if (text.isBlank()) emptyList()
                else json.decodeFromString(Wrapper.serializer(), text).items
            }
        }
    }

    suspend fun writeAll(items: List<Character>) = withContext(Dispatchers.IO) {
        lock.withLock {
            val text = json.encodeToString(Wrapper.serializer(), Wrapper(items))
            file.writeText(text)
        }
    }
}