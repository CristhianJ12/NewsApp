// ==========================================
// 📄 ARCHIVO: ObtenerEstadisticasUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.repository.EstadisticasNoticias
import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

class ObtenerEstadisticasUseCase @Inject constructor(
    private val repository: NoticiasRepository
) {
    suspend operator fun invoke() = repository.obtenerEstadisticas()
}