// ==========================================
// 📄 ARCHIVO: NoticiasRepository.kt
// 📁 UBICACIÓN: domain/repository/
// 🔷 TIPO: Interface
// ==========================================

package com.example.newsapp.domain.repository

import com.example.newsapp.domain.model.DocumentoCTI
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de noticias
 * Define las operaciones disponibles para acceder a noticias
 */
interface NoticiasRepository {

    // ===== OBSERVABLES =====

    fun observarNoticias(): Flow<List<DocumentoCTI>>
    fun observarPorCategoria(categoria: String): Flow<List<DocumentoCTI>>
    fun observarGuardadas(): Flow<List<DocumentoCTI>>

    // ===== BÚSQUEDAS =====

    suspend fun buscarPorTexto(texto: String, limite: Int = 20): List<DocumentoCTI>
    suspend fun obtenerRecientes(horas: Int = 24): List<DocumentoCTI>
    suspend fun obtenerPorId(id: String): DocumentoCTI?
    suspend fun buscarPorKeyword(keyword: String): List<DocumentoCTI>

    // ===== INGESTA =====

    suspend fun actualizarNoticias(): Result<Int>
    suspend fun insertarNoticias(noticias: List<DocumentoCTI>): Result<Unit>

    // ===== MODIFICACIONES =====

    suspend fun marcarComoConsultada(id: String): Result<Unit>
    suspend fun guardarNoticia(noticia: DocumentoCTI): Result<Unit>
    suspend fun eliminarGuardada(id: String): Result<Unit>
    suspend fun actualizarResumen(id: String, resumen: String): Result<Unit>

    // ===== LIMPIEZA =====

    suspend fun limpiarNoticiasAntiguas(): Result<Int>
    suspend fun obtenerEstadisticas(): EstadisticasNoticias
}

// ==========================================
// FIN DE ARCHIVO NoticiasRepository.kt
// ==========================================