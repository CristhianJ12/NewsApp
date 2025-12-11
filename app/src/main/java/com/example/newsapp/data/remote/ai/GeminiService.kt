package com.example.newsapp.data.remote.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para interactuar con Gemini AI
 * Maneja toda la comunicación con la API de Google Gemini
 *
 * COSTO: ~$0.0002 por consulta usando Gemini Flash
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
     * CORRECCIÓN: Usar el nombre correcto del modelo
     */
    private fun obtenerModelo(): GenerativeModel {
        if (model == null && apiKey.isNotBlank()) {
            model = GenerativeModel(
                modelName = "gemini-1.5-flash-latest", // ✅ Nombre correcto del modelo
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f  // Balance entre creatividad y precisión
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 500 // Limitar respuesta para controlar costos
                }
            )
        }
        return model ?: throw IllegalStateException("API key no configurada")
    }

    /**
     * Verifica si el servicio está configurado
     */
    fun estaConfigurado(): Boolean {
        return apiKey.isNotBlank()
    }

    /**
     * Genera una respuesta conversacional basada en el contexto
     *
     * @param contexto Información relevante encontrada en la BD
     * @param consulta Pregunta del usuario
     * @param promptSistema Instrucciones del sistema (opcional)
     * @return Respuesta generada por la IA
     */
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
                Result.failure(Exception("Gemini no generó respuesta"))
            } else {
                Result.success(textoRespuesta.trim())
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al comunicar con Gemini: ${e.message}", e))
        }
    }

    /**
     * Genera un resumen ejecutivo de un documento
     *
     * @param contenido Contenido completo del documento
     * @return Resumen de máximo 100 palabras
     */
    suspend fun generarResumen(contenido: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelo = obtenerModelo()

            val prompt = """
                Resume el siguiente texto en EXACTAMENTE 100 palabras o menos.
                El resumen debe ser claro, directo y estilo periodístico.
                Estructura: QUÉ ocurrió, QUIÉN está involucrado, CÓMO afecta.
                
                TEXTO:
                ${contenido.take(3000)}
                
                RESUMEN (máximo 100 palabras):
            """.trimIndent()

            val response = modelo.generateContent(prompt)
            val resumen = response.text ?: ""

            if (resumen.isBlank()) {
                Result.failure(Exception("No se pudo generar resumen"))
            } else {
                Result.success(resumen.trim())
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al generar resumen: ${e.message}", e))
        }
    }

    /**
     * Detecta la intención de la consulta del usuario
     *
     * @param consulta Texto de la consulta
     * @return Intención detectada en formato JSON
     */
    suspend fun detectarIntencion(consulta: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val modelo = obtenerModelo()

            val prompt = """
                Analiza esta consulta y determina la intención del usuario.
                
                CONSULTA: "$consulta"
                
                Responde SOLO con una de estas opciones:
                - "resumen_dia" si pregunta por noticias generales de hoy
                - "buscar_categoria:NOMBRE" si busca una categoría específica
                - "buscar_texto:TEXTO" si busca algo específico
                - "configurar:TIPO" si quiere configurar algo
                - "no_reconocida" si no entiendes
                
                RESPUESTA:
            """.trimIndent()

            val response = modelo.generateContent(prompt)
            val intencion = response.text ?: "no_reconocida"

            Result.success(intencion.trim())

        } catch (e: Exception) {
            Result.failure(Exception("Error al detectar intención: ${e.message}", e))
        }
    }

    companion object {
        /**
         * Prompt del sistema por defecto
         * Define el comportamiento general del asistente
         */
        private const val PROMPT_SISTEMA_DEFAULT = """
Eres un asistente de noticias peruano conversacional e inteligente.

TUS CAPACIDADES:
1. INTERPRETAR: Entender qué información busca el usuario
2. BUSCAR: En los documentos que te proporciono como contexto
3. RESUMIR: De forma clara, concisa y conversacional
4. SUGERIR: Acciones útiles para el usuario

REGLAS IMPORTANTES:
- Responde SOLO basándote en el contexto proporcionado
- Si no tienes información, dilo claramente
- Sé conciso: máximo 150 palabras por respuesta
- Usa español peruano natural y profesional
- Si hay varias noticias, menciona las 3 más relevantes
- Pregunta si el usuario quiere más detalles
- No inventes información que no esté en el contexto
- Si el usuario menciona un nombre o término que no está en el contexto, 
  dile que no encontraste información sobre eso

ESTILO:
- Natural y conversacional (como hablar con un colega)
- Directo y sin rodeos
- Profesional pero amigable
"""
    }
}