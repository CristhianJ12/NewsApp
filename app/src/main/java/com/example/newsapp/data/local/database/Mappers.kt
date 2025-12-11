package com.example.newsapp.data.local.database

import com.example.newsapp.data.local.database.entities.ConfiguracionEntity
import com.example.newsapp.data.local.database.entities.NoticiaEntity
import com.example.newsapp.data.local.database.entities.NoticiaGuardadaEntity
import com.example.newsapp.domain.model.ConfiguracionUsuario
import com.example.newsapp.domain.model.DiaSemana
import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.model.PreferenciaDia
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Mappers para convertir entre entidades de Room y modelos de dominio
 */

private val gson = Gson()

// ===== DocumentoCTI <-> NoticiaEntity =====

fun DocumentoCTI.toEntity(): NoticiaEntity {
    return NoticiaEntity(
        id = id,
        titulo = titulo,
        contenidoCompleto = contenidoCompleto,
        resumenEjecutivo = resumenEjecutivo,
        categoria = categoria,
        diario = diario,
        entidadResponsable = entidadResponsable,
        palabrasClave = gson.toJson(palabrasClave),
        fechaPublicacion = fechaPublicacion,
        fechaIngesta = fechaIngesta,
        urlOriginal = urlOriginal,
        fueConsultado = fueConsultado,
        vecesConsultado = vecesConsultado,
        esGuardado = esGuardado
    )
}

fun NoticiaEntity.toDomain(): DocumentoCTI {
    val palabrasClaveList: List<String> = try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(palabrasClave, type)
    } catch (e: Exception) {
        emptyList()
    }

    return DocumentoCTI(
        id = id,
        titulo = titulo,
        contenidoCompleto = contenidoCompleto,
        resumenEjecutivo = resumenEjecutivo,
        categoria = categoria,
        diario = diario,
        entidadResponsable = entidadResponsable,
        palabrasClave = palabrasClaveList,
        fechaPublicacion = fechaPublicacion,
        fechaIngesta = fechaIngesta,
        urlOriginal = urlOriginal,
        fueConsultado = fueConsultado,
        vecesConsultado = vecesConsultado,
        esGuardado = esGuardado
    )
}

fun List<NoticiaEntity>.toDomain(): List<DocumentoCTI> {
    return map { it.toDomain() }
}

// ===== DocumentoCTI <-> NoticiaGuardadaEntity =====

fun DocumentoCTI.toGuardadaEntity(
    tags: List<String> = emptyList(),
    recordatorioFecha: Long? = null,
    recordatorioMensaje: String? = null
): NoticiaGuardadaEntity {
    return NoticiaGuardadaEntity(
        id = id,
        titulo = titulo,
        resumenEjecutivo = resumenEjecutivo ?: contenidoCompleto.take(200),
        categoria = categoria,
        diario = diario,
        fechaPublicacion = fechaPublicacion,
        fechaGuardado = System.currentTimeMillis(),
        urlOriginal = urlOriginal,
        tags = gson.toJson(tags),
        recordatorioFecha = recordatorioFecha,
        recordatorioMensaje = recordatorioMensaje
    )
}

fun NoticiaGuardadaEntity.toDomain(): DocumentoCTI {
    return DocumentoCTI(
        id = id,
        titulo = titulo,
        contenidoCompleto = resumenEjecutivo,
        resumenEjecutivo = resumenEjecutivo,
        categoria = categoria,
        diario = diario,
        palabrasClave = emptyList(),
        fechaPublicacion = fechaPublicacion,
        fechaIngesta = fechaGuardado,
        urlOriginal = urlOriginal,
        fueConsultado = false,
        vecesConsultado = 0,
        esGuardado = true
    )
}

// ===== ConfiguracionUsuario <-> ConfiguracionEntity =====

fun ConfiguracionUsuario.toEntity(): ConfiguracionEntity {
    return ConfiguracionEntity(
        id = id,
        preferenciasSemanalesJson = serializarPreferenciasSemanales(preferenciasSemanales),
        categoriasExcluidasJson = gson.toJson(categoriasExcluidas),
        diariosPreferidosJson = gson.toJson(diariosPreferidos),
        keywordsSeguimientoJson = gson.toJson(keywordsSeguimiento),
        alertasActivas = alertasActivas,
        horaConsolidacionMañana = horaConsolidacionMañana,
        horaConsolidacionTarde = horaConsolidacionTarde,
        fechaActualizacion = System.currentTimeMillis()
    )
}

fun ConfiguracionEntity.toDomain(): ConfiguracionUsuario {
    return ConfiguracionUsuario(
        id = id,
        preferenciasSemanales = deserializarPreferenciasSemanales(preferenciasSemanalesJson),
        categoriasExcluidas = gson.fromJson(
            categoriasExcluidasJson,
            object : TypeToken<List<String>>() {}.type
        ) ?: emptyList(),
        diariosPreferidos = gson.fromJson(
            diariosPreferidosJson,
            object : TypeToken<List<String>>() {}.type
        ) ?: emptyList(),
        keywordsSeguimiento = gson.fromJson(
            keywordsSeguimientoJson,
            object : TypeToken<List<String>>() {}.type
        ) ?: emptyList(),
        alertasActivas = alertasActivas,
        horaConsolidacionMañana = horaConsolidacionMañana,
        horaConsolidacionTarde = horaConsolidacionTarde
    )
}

// ===== Helpers para serialización compleja =====

private fun serializarPreferenciasSemanales(
    preferencias: Map<DiaSemana, PreferenciaDia>
): String {
    // Convertir Map<DiaSemana, PreferenciaDia> a Map<String, PreferenciaDia>
    val stringMap = preferencias.mapKeys { it.key.name }
    return gson.toJson(stringMap)
}

private fun deserializarPreferenciasSemanales(
    json: String
): Map<DiaSemana, PreferenciaDia> {
    return try {
        val type = object : TypeToken<Map<String, PreferenciaDia>>() {}.type
        val stringMap: Map<String, PreferenciaDia> = gson.fromJson(json, type)

        // Convertir Map<String, PreferenciaDia> a Map<DiaSemana, PreferenciaDia>
        stringMap.mapKeys { DiaSemana.valueOf(it.key) }
    } catch (e: Exception) {
        emptyMap()
    }
}