// ==========================================
// 📄 ARCHIVO: BuscarNoticiasUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

class BuscarNoticiasUseCase @Inject constructor(
    private val repository: NoticiasRepository
) {
    suspend operator fun invoke(query: String, limite: Int = 20): List<DocumentoCTI> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            repository.buscarPorTexto(query, limite)
        }
    }
}