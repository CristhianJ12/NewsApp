// ==========================================
// 📄 ARCHIVO: ConfiguracionDao.kt
// 📁 UBICACIÓN: data/local/database/dao/
// 🔷 TIPO: Interface (DAO)
// ==========================================

package com.example.newsapp.data.local.database.dao

import androidx.room.*
import com.example.newsapp.data.local.database.entities.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para configuración del usuario (persistente)
 */
@Dao
interface ConfiguracionDao {

    // ===== OBSERVABLES =====

    @Query("SELECT * FROM configuracion_usuario WHERE id = 'default' LIMIT 1")
    fun observarConfiguracion(): Flow<ConfiguracionEntity?>

    // ===== CONSULTAS =====

    @Query("SELECT * FROM configuracion_usuario WHERE id = 'default' LIMIT 1")
    suspend fun obtenerConfiguracion(): ConfiguracionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM configuracion_usuario WHERE id = 'default')")
    suspend fun existeConfiguracion(): Boolean

    // ===== INSERCIONES/ACTUALIZACIONES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarConfiguracion(config: ConfiguracionEntity)

    @Update
    suspend fun actualizarConfiguracion(config: ConfiguracionEntity)

    // ===== ELIMINACIONES =====

    @Query("DELETE FROM configuracion_usuario WHERE id = 'default'")
    suspend fun eliminarConfiguracion()

    @Query("DELETE FROM configuracion_usuario")
    suspend fun resetearConfiguracion()
}

// ==========================================
// FIN DE ARCHIVO ConfiguracionDao.kt
// ==========================================