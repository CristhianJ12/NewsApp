// ==========================================
// 📄 ARCHIVO: NewsDatabase.kt
// 📁 UBICACIÓN: data/local/database/
// 🗄️ TIPO: Database (Room)
// ==========================================

package com.example.newsapp.data.local.database

import NoticiasGuardadasDao
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.data.local.database.dao.ConfiguracionDao
import com.example.newsapp.data.local.database.dao.NoticiasDao
import com.example.newsapp.data.local.database.entities.ConfiguracionEntity
import com.example.newsapp.data.local.database.entities.Converters
import com.example.newsapp.data.local.database.entities.NoticiaEntity

/**
 * Base de datos principal de la aplicación
 *
 * Versión 1: Setup inicial con 3 tablas
 * - noticias_dia: Noticias del día (eliminadas después de 24h)
 * - noticias_guardadas: Noticias guardadas por el usuario (persistentes)
 * - configuracion_usuario: Preferencias del usuario (persistente)
 */
@Database(
    entities = [
        NoticiaEntity::class,
        NoticiaGuardadaEntity::class,
        ConfiguracionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun noticiasDao(): NoticiasDao
    abstract fun noticiasGuardadasDao(): NoticiasGuardadasDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        const val DATABASE_NAME = "news_database"
    }
}