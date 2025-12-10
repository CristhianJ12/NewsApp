// ==========================================
// 📄 ARCHIVO: EstadisticasNoticias.kt
// 📁 UBICACIÓN: domain/repository/
// 📘 TIPO: Data class
// ==========================================

package com.example.newsapp.domain.repository

/**
 * Datos estadísticos de noticias
 */
data class EstadisticasNoticias(
    val totalNoticias: Int = 0,
    val noticiasRecientes: Int = 0,
    val noticiasGuardadas: Int = 0,
    val porCategoria: Map<String, Int> = emptyMap(),
    val ultimaActualizacion: Long = 0L
)

// ==========================================
// FIN DE ARCHIVO EstadisticasNoticias.kt
// ==========================================