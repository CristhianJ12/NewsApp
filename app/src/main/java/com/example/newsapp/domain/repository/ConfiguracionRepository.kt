package com.example.newsapp.domain.repository

import com.example.newsapp.domain.model.ConfiguracionUsuario
import com.example.newsapp.domain.model.DiaSemana
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de configuración
 * Maneja las preferencias del usuario
 */
interface ConfiguracionRepository {

    /**
     * Observa la configuración del usuario
     */
    fun observarConfiguracion(): Flow<ConfiguracionUsuario?>

    /**
     * Obtiene la configuración actual
     */
    suspend fun obtenerConfiguracion(): ConfiguracionUsuario

    /**
     * Guarda la configuración completa
     */
    suspend fun guardarConfiguracion(config: ConfiguracionUsuario): Result<Unit>

    /**
     * Configura preferencias para un día específico
     */
    suspend fun configurarDia(
        dia: DiaSemana,
        categorias: List<String>,
        modoExclusivo: Boolean
    ): Result<Unit>

    /**
     * Excluye una categoría de las noticias
     */
    suspend fun excluirCategoria(categoria: String): Result<Unit>

    /**
     * Agrega una keyword para seguimiento
     */
    suspend fun seguirKeyword(keyword: String): Result<Unit>

    /**
     * Marca un diario como preferido
     */
    suspend fun preferirDiario(diario: String): Result<Unit>

    /**
     * Obtiene las categorías activas para hoy
     */
    suspend fun obtenerCategoriasActivasHoy(): List<String>

    /**
     * Verifica si una categoría está excluida
     */
    suspend fun estaExcluida(categoria: String): Boolean
}

/**
 * Datos estadísticos de noticias
 */