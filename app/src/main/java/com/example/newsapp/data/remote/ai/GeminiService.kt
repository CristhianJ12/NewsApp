package com.example.newsapp.data.remote.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para interactuar con Gemini AI
 */
@Singleton
class GeminiService @Inject constructor(
    private var apiKey: String
) {

    private var model: GenerativeModel? = null

    /**
     * Configura la API key y reinicia el modelo
     */
    fun configurarApiKey(newApiKey: String) {
        apiKey = newApiKey
        model = null // Forzar recreación
    }

    /**
     * Obtiene o crea el modelo de Gemini
     */
    private fun obtenerModelo(): GenerativeModel {
        if (model == null && apiKey.isNotBlank()) {
            model = GenerativeModel(
                modelName = "gemini-pro", // Modelo moderno y estable
                apiKey = apiKey,
                generationConfig = generationConfig {
                    // Propiedad 'temperature'
                    temperature = 0.7f
                    // Propiedades Top-K y Top-P
                    topK = 40
                    topP = 0.95f
                    // El límite de tokens se llama 'maxOutputTokens'
                    maxOutputTokens = 1024
                }
            )
        }
        // Este throw IllegalStateException garantiza que no se use un modelo nulo
        return model ?: throw IllegalStateException("API key no configurada")
    }

    fun estaConfigurado(): Boolean {
        return apiKey.isNotBlank()
    }

    // --- EL RESTO DE LAS FUNCIONES ---

    suspend fun generarRespuesta(
        contexto: String,
        consulta: String,
        promptSistema: String = PROMPT_SISTEMA_DEFAULT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelo = obtenerModelo()

            val promptCompleto = buildString {
                append(promptSistema)
                append("\n\n")
                append("CONTEXTO DISPONIBLE:\n")
                append(contexto)
                append("\n\n")
                append("CONSULTA DEL USUARIO:\n")
                append(consulta)
                append("\n\n")
                append("RESPUESTA:")
            }

            val response = modelo.generateContent(promptCompleto)
            val textoRespuesta = response.text ?: ""

            if (textoRespuesta.isBlank()) {
                Result.failure(Exception("Gemini no generó respuesta para la consulta: $consulta"))
            } else {
                Result.success(textoRespuesta.trim())
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al comunicar con Gemini: ${e.message}", e))
        }
    }

    // NOTA: Debes agregar aquí tus funciones 'generarResumen' y 'detectarIntencion'

    companion object {
        // CORRECCIÓN: Definición del constante que faltaba
        private const val PROMPT_SISTEMA_DEFAULT = """
            Eres un asistente de noticias peruano conversacional e inteligente.
            Tu propósito es responder preguntas basadas en el contexto proporcionado.
            Sé conciso, profesional y responde en español.
        """
    }

    suspend fun generarResumen(contenido: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelo = obtenerModelo()

            // Define el prompt del sistema para el resumen
            val sistemaPrompt = """
                Eres un asistente de noticias experto en resumir. Tu tarea es generar un resumen
                ejecutivo del contenido proporcionado.
                El resumen debe tener una longitud máxima de 100 palabras.
                Estructura tu respuesta de forma profesional.
            """.trimIndent()

            val usuarioPrompt = "TEXTO A RESUMIR:\n${contenido}\n\nRESUMEN (máximo 100 palabras):"

            val promptCompleto = "$sistemaPrompt\n\n$usuarioPrompt"

            val response = modelo.generateContent(promptCompleto)
            val textoRespuesta = response.text ?: ""

            if (textoRespuesta.isBlank()) {
                Result.failure(Exception("Gemini no generó resumen."))
            } else {
                Result.success(textoRespuesta.trim())
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al generar resumen: ${e.message}", e))
        }
    }


    /**
     * Detecta la intención del usuario a partir de una consulta de texto.
     * (Función necesaria si se usa en otros UseCases)
     */
    suspend fun detectarIntencion(consulta: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelo = obtenerModelo()

            val sistemaPrompt = """
                Analiza la consulta del usuario y determina su intención. Responde ÚNICA y
                EXCLUSIVAMENTE con el formato predefinido.
                Opciones de respuesta (SOLO UNA):
                - "resumen_dia"
                - "buscar_categoria:NOMBRE_CATEGORIA"
                - "buscar_texto:TEXTO_BUSQUEDA"
                - "configurar:TIPO_CONFIG"
                - "no_reconocida"
                No añadas explicaciones, comillas o texto adicional.
            """.trimIndent()

            val usuarioPrompt = "CONSULTA: \"$consulta\"\n\nRESPUESTA ÚNICA Y DIRECTA:"

            val promptCompleto = "$sistemaPrompt\n\n$usuarioPrompt"

            val response = modelo.generateContent(promptCompleto)
            val textoRespuesta = response.text ?: ""

            if (textoRespuesta.isBlank()) {
                Result.success("no_reconocida")
            } else {
                Result.success(textoRespuesta.trim())
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al detectar intención: ${e.message}", e))
        }
    }
}