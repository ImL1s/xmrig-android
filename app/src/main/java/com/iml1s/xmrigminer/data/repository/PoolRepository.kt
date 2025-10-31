package com.iml1s.xmrigminer.data.repository

import android.content.Context
import com.iml1s.xmrigminer.data.model.Pool
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoolRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getPools(): List<Pool> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("pools.json")
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString<List<Pool>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
