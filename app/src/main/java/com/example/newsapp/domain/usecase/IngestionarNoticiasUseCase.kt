// ==========================================
// 📄 ARCHIVO: IngestionarNoticiasUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

class IngestionarNoticiasUseCase @Inject constructor(
    private val repository: NoticiasRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.actualizarNoticias()
    }
}