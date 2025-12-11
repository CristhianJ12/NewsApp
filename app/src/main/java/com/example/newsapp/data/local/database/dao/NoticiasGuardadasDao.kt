package com.example.newsapp.data.local.database.dao

import androidx.room.*
import com.example.newsapp.data.local.database.entities.NoticiaGuardadaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para noticias guardadas (persistentes)
 */
@Dao
interface NoticiasGuardadasDao {

    // ===== OBSERVABLES =====

    @Query("SELECT * FROM noticias_guardadas ORDER BY fechaGuardado DESC")
    fun observarGuardadas(): Flow<List<NoticiaGuardadaEntity>>

    @Query("SELECT * FROM noticias_guardadas WHERE categoria = :categoria ORDER BY fechaGuardado DESC")
    fun observarGuardadasPorCategoria(categoria: String): Flow<List<NoticiaGuardadaEntity>>

    // ===== CONSULTAS =====

    @Query("SELECT * FROM noticias_guardadas WHERE id = :id")
    suspend fun obtenerPorId(id: String): NoticiaGuardadaEntity?

    @Query("SELECT COUNT(*) FROM noticias_guardadas")
    suspend fun contarGuardadas(): Int

    // ===== INSERCIONES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(noticia: NoticiaGuardadaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(noticias: List<NoticiaGuardadaEntity>)

    // ===== ACTUALIZACIONES =====

    @Update
    suspend fun actualizar(noticia: NoticiaGuardadaEntity)

    // ===== ELIMINACIONES =====

    @Query("DELETE FROM noticias_guardadas WHERE id = :id")
    suspend fun eliminarPorId(id: String)

    @Delete
    suspend fun eliminar(noticia: NoticiaGuardadaEntity)

    @Query("DELETE FROM noticias_guardadas")
    suspend fun eliminarTodas()
}