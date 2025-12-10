// ==========================================
// 📄 ARCHIVO: GenerarResumenUseCase.kt
// 📁 UBICACIÓN: domain/usecase/
// ==========================================
package com.example.newsapp.domain.usecase

import com.example.newsapp.data.remote.ai.GeminiService
import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

class GenerarResumenUseCase @Inject constructor(
    private val geminiService: GeminiService,
    private val repository: NoticiasRepository
) {
    suspend operator fun invoke(noticia: DocumentoCTI): Result<String> {
        return try {
            if (!geminiService.estaConfigurado()) {
                return Result.failure(Exception("Gemini no está configurado"))
            }

            val resultado = geminiService.generarResumen(noticia.contenidoCompleto)

            resultado.onSuccess { resumen ->
                repository.actualizarResumen(noticia.id, resumen)
            }

            resultado
        } catch (e: Exception) {
            Result.failure(Exception("Error al generar resumen: ${e.message}", e))
        }
    }
}