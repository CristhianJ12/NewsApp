// ==========================================
// 📄 ARCHIVO: GuardarNoticiaUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

class GuardarNoticiaUseCase @Inject constructor(
    private val repository: NoticiasRepository
) {
    suspend operator fun invoke(noticia: DocumentoCTI): Result<Unit> {
        return repository.guardarNoticia(noticia)
    }
}