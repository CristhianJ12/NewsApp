// ==========================================
// 📄 ARCHIVO: Converters.kt
// 📁 UBICACIÓN: data/local/database/entities/
// 🟢 TIPO: Class (TypeConverters para Room)
// ==========================================

package com.example.newsapp.data.local.database.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * TypeConverters para Room
 * Convierte tipos complejos a tipos que Room puede guardar
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }
}

// ==========================================
// FIN DE ARCHIVO Converters.kt
// ==========================================