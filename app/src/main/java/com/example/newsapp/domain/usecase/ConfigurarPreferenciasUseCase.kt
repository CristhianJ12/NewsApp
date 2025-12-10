// ==========================================
// 📄 ARCHIVO: ConfigurarPreferenciasUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.model.ConfiguracionUsuario
import com.example.newsapp.domain.model.DiaSemana
import com.example.newsapp.domain.repository.ConfiguracionRepository
import javax.inject.Inject

class ConfigurarPreferenciasUseCase @Inject constructor(
    private val repository: ConfiguracionRepository
) {
    suspend fun configurarDia(
        dia: DiaSemana,
        categorias: List<String>,
        modoExclusivo: Boolean = true
    ): Result<Unit> {
        return repository.configurarDia(dia, categorias, modoExclusivo)
    }

    suspend fun excluirCategoria(categoria: String): Result<Unit> {
        return repository.excluirCategoria(categoria)
    }

    suspend fun seguirKeyword(keyword: String): Result<Unit> {
        return repository.seguirKeyword(keyword)
    }

    suspend fun obtenerConfiguracion(): ConfiguracionUsuario {
        return repository.obtenerConfiguracion()
    }
}