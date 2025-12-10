package com.example.newsapp.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para configuración del usuario
 */
@Entity(tableName = "configuracion_usuario")
data class ConfiguracionEntity(
    @PrimaryKey
    val id: String = "default",
    val preferenciasSemanalesJson: String, // JSON de Map<DiaSemana, PreferenciaDia>
    val categoriasExcluidasJson: String,   // JSON de List<String>
    val diariosPreferidosJson: String,     // JSON de List<String>
    val keywordsSeguimientoJson: String,   // JSON de List<String>
    val alertasActivas: Boolean,
    val horaConsolidacionMañana: String,
    val horaConsolidacionTarde: String,
    val fechaActualizacion: Long
)
