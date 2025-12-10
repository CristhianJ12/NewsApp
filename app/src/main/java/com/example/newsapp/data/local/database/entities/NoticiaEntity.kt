package com.example.newsapp.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Entidad para noticias del día (se eliminan después de 24h)
 */
@Entity(tableName = "noticias_dia")
data class NoticiaEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val contenidoCompleto: String,
    val resumenEjecutivo: String?,
    val categoria: String,
    val diario: String,
    val entidadResponsable: String?,
    val palabrasClave: String, // JSON string de List<String>
    val fechaPublicacion: Long,
    val fechaIngesta: Long,
    val urlOriginal: String,
    val fueConsultado: Boolean,
    val vecesConsultado: Int,
    val esGuardado: Boolean
)

