package com.example.newsapp.data.repository

import com.example.newsapp.data.local.database.dao.ConfiguracionDao
import com.example.newsapp.data.local.database.toDomain
import com.example.newsapp.data.local.database.toEntity
import com.example.newsapp.domain.model.ConfiguracionUsuario
import com.example.newsapp.domain.model.DiaSemana
import com.example.newsapp.domain.model.PreferenciaDia
import com.example.newsapp.domain.repository.ConfiguracionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de configuración
 */
@Singleton
class ConfiguracionRepositoryImpl @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : ConfiguracionRepository {

    override fun observarConfiguracion(): Flow<ConfiguracionUsuario?> {
        return configuracionDao.observarConfiguracion()
            .map { entity -> entity?.toDomain() }
    }

    override suspend fun obtenerConfiguracion(): ConfiguracionUsuario {
        val entity = configuracionDao.obtenerConfiguracion()
        return entity?.toDomain() ?: ConfiguracionUsuario() // Configuración por defecto
    }

    override suspend fun guardarConfiguracion(config: ConfiguracionUsuario): Result<Unit> {
        return try {
            configuracionDao.guardarConfiguracion(config.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar configuración: ${e.message}", e))
        }
    }

    override suspend fun configurarDia(
        dia: DiaSemana,
        categorias: List<String>,
        modoExclusivo: Boolean
    ): Result<Unit> {
        return try {
            val configActual = obtenerConfiguracion()

            val nuevaPreferencia = PreferenciaDia(
                categoriasActivas = categorias,
                modoExclusivo = modoExclusivo
            )

            val nuevasPreferencias = configActual.preferenciasSemanales.toMutableMap()
            nuevasPreferencias[dia] = nuevaPreferencia

            val nuevaConfig = configActual.copy(
                preferenciasSemanales = nuevasPreferencias
            )

            guardarConfiguracion(nuevaConfig)
        } catch (e: Exception) {
            Result.failure(Exception("Error al configurar día: ${e.message}", e))
        }
    }

    override suspend fun excluirCategoria(categoria: String): Result<Unit> {
        return try {
            val configActual = obtenerConfiguracion()

            val nuevasExcluidas = configActual.categoriasExcluidas.toMutableList()
            if (!nuevasExcluidas.contains(categoria)) {
                nuevasExcluidas.add(categoria)
            }

            val nuevaConfig = configActual.copy(
                categoriasExcluidas = nuevasExcluidas
            )

            guardarConfiguracion(nuevaConfig)
        } catch (e: Exception) {
            Result.failure(Exception("Error al excluir categoría: ${e.message}", e))
        }
    }

    override suspend fun seguirKeyword(keyword: String): Result<Unit> {
        return try {
            val configActual = obtenerConfiguracion()

            val nuevasKeywords = configActual.keywordsSeguimiento.toMutableList()
            if (!nuevasKeywords.contains(keyword)) {
                nuevasKeywords.add(keyword)
            }

            val nuevaConfig = configActual.copy(
                keywordsSeguimiento = nuevasKeywords
            )

            guardarConfiguracion(nuevaConfig)
        } catch (e: Exception) {
            Result.failure(Exception("Error al seguir keyword: ${e.message}", e))
        }
    }

    override suspend fun preferirDiario(diario: String): Result<Unit> {
        return try {
            val configActual = obtenerConfiguracion()

            val nuevosDiarios = configActual.diariosPreferidos.toMutableList()
            if (!nuevosDiarios.contains(diario)) {
                nuevosDiarios.add(diario)
            }

            val nuevaConfig = configActual.copy(
                diariosPreferidos = nuevosDiarios
            )

            guardarConfiguracion(nuevaConfig)
        } catch (e: Exception) {
            Result.failure(Exception("Error al preferir diario: ${e.message}", e))
        }
    }

    override suspend fun obtenerCategoriasActivasHoy(): List<String> {
        val config = obtenerConfiguracion()
        val hoy = DiaSemana.obtenerDiaActual()

        val preferenciaHoy = config.preferenciasSemanales[hoy]

        return if (preferenciaHoy != null && preferenciaHoy.modoExclusivo) {
            // Si hay modo exclusivo, solo esas categorías
            preferenciaHoy.categoriasActivas
        } else {
            // Si no, todas excepto las excluidas
            com.example.newsapp.domain.model.Categorias.TODAS
                .filter { !config.categoriasExcluidas.contains(it) }
        }
    }

    override suspend fun estaExcluida(categoria: String): Boolean {
        val config = obtenerConfiguracion()
        return config.categoriasExcluidas.contains(categoria)
    }
}