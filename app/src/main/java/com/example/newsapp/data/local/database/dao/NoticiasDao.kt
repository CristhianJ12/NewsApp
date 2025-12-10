// ==========================================
// 📄 ARCHIVO: NoticiasDao.kt
// 📁 UBICACIÓN: data/local/database/dao/
// 🔷 TIPO: Interface (DAO)
// ==========================================

package com.example.newsapp.data.local.database.dao

import androidx.room.*
import com.example.newsapp.data.local.database.entities.NoticiaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para noticias del día (se eliminan después de 24h)
 */
@Dao
interface NoticiasDao {

    // ===== OBSERVABLES (Flow) =====

    @Query("SELECT * FROM noticias_dia ORDER BY fechaPublicacion DESC")
    fun observarTodasLasNoticias(): Flow<List<NoticiaEntity>>

    @Query("SELECT * FROM noticias_dia WHERE categoria = :categoria ORDER BY fechaPublicacion DESC")
    fun observarPorCategoria(categoria: String): Flow<List<NoticiaEntity>>

    // ===== CONSULTAS (Suspend) =====

    @Query("SELECT * FROM noticias_dia WHERE fechaPublicacion >= :timestamp ORDER BY fechaPublicacion DESC")
    suspend fun obtenerRecientes(timestamp: Long): List<NoticiaEntity>

    @Query("SELECT * FROM noticias_dia WHERE id = :id")
    suspend fun obtenerPorId(id: String): NoticiaEntity?

    @Query("""
        SELECT * FROM noticias_dia 
        WHERE titulo LIKE '%' || :texto || '%' 
        OR contenidoCompleto LIKE '%' || :texto || '%'
        OR palabrasClave LIKE '%' || :texto || '%'
        ORDER BY fechaPublicacion DESC
        LIMIT :limite
    """)
    suspend fun buscarPorTexto(texto: String, limite: Int = 20): List<NoticiaEntity>

    @Query("SELECT COUNT(*) FROM noticias_dia")
    suspend fun contarNoticias(): Int

    @Query("SELECT COUNT(*) FROM noticias_dia WHERE categoria = :categoria")
    suspend fun contarPorCategoria(categoria: String): Int

    // ===== INSERCIONES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(noticia: NoticiaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(noticias: List<NoticiaEntity>)

    // ===== ACTUALIZACIONES =====

    @Query("""
        UPDATE noticias_dia 
        SET fueConsultado = :consultado, 
            vecesConsultado = vecesConsultado + 1 
        WHERE id = :id
    """)
    suspend fun marcarComoConsultado(id: String, consultado: Boolean = true)

    @Query("UPDATE noticias_dia SET resumenEjecutivo = :resumen WHERE id = :id")
    suspend fun actualizarResumen(id: String, resumen: String)

    @Query("UPDATE noticias_dia SET esGuardado = :guardado WHERE id = :id")
    suspend fun actualizarGuardado(id: String, guardado: Boolean)

    @Update
    suspend fun actualizar(noticia: NoticiaEntity)

    // ===== ELIMINACIONES =====

    @Query("""
        DELETE FROM noticias_dia 
        WHERE fechaPublicacion < :timestamp 
        AND esGuardado = 0
    """)
    suspend fun eliminarAnteriores(timestamp: Long): Int

    @Query("DELETE FROM noticias_dia WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Query("DELETE FROM noticias_dia")
    suspend fun eliminarTodas()

    @Delete
    suspend fun eliminar(noticia: NoticiaEntity)
}

// ==========================================
// FIN DE ARCHIVO NoticiasDao.kt
// ==========================================