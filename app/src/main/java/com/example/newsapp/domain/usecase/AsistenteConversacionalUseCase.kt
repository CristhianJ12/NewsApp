package com.example.newsapp.domain.usecase

import com.example.newsapp.data.remote.ai.GeminiService
import com.example.newsapp.domain.model.*
import com.example.newsapp.domain.repository.ConfiguracionRepository
import com.example.newsapp.domain.repository.NoticiasRepository
import javax.inject.Inject

/**
 * Caso de uso principal: Asistente conversacional
 *
 * Este es el cerebro de la aplicación. Procesa consultas del usuario
 * y genera respuestas inteligentes usando Gemini AI.
 *
 * COSTO: ~$0.0002 por consulta
 */
class AsistenteConversacionalUseCase @Inject constructor(
    private val noticiasRepository: NoticiasRepository,
    private val configuracionRepository: ConfiguracionRepository,
    private val geminiService: GeminiService
) {

    /**
     * Procesa una consulta del usuario
     *
     * FLUJO:
     * 1. Detecta la intención de la consulta
     * 2. Busca información relevante en la BD
     * 3. Construye contexto para la IA
     * 4. Genera respuesta con Gemini
     * 5. Retorna respuesta estructurada
     */
    suspend operator fun invoke(
        consulta: String,
        contextoConversacion: ContextoConversacion = ContextoConversacion()
    ): Result<RespuestaAsistente> {
        return try {
            // Verificar que Gemini esté configurado
            if (!geminiService.estaConfigurado()) {
                return Result.success(
                    RespuestaAsistente(
                        textoRespuesta = "Para usar el asistente, necesitas configurar tu API key de Gemini. " +
                                "Ve a Configuración y agrega tu clave.",
                        tipoRespuesta = TipoRespuesta.ERROR,
                        accionSugerida = AccionSugerida.CONFIGURAR_PREFERENCIAS
                    )
                )
            }

            // 1. Detectar intención
            val intencion = detectarIntencion(consulta)

            // 2. Buscar información relevante
            val documentos = buscarDocumentosRelevantes(intencion, consulta)

            // 3. Manejar casos especiales
            when {
                documentos.isEmpty() && intencion !is IntencionConsulta.ConfigurarDia -> {
                    return Result.success(
                        RespuestaAsistente(
                            textoRespuesta = "No encontré información sobre eso. " +
                                    "¿Quieres que actualice las noticias?",
                            tipoRespuesta = TipoRespuesta.CONSULTA_VACIA,
                            accionSugerida = AccionSugerida.ACTUALIZAR_FUENTES
                        )
                    )
                }
            }

            // 4. Construir contexto para IA
            val contexto = construirContexto(documentos, consulta, contextoConversacion)

            // 5. Generar respuesta con Gemini
            val respuestaIA = geminiService.generarRespuesta(
                contexto = contexto,
                consulta = consulta
            )

            respuestaIA.map { texto ->
                // Marcar documentos como consultados
                documentos.forEach { doc ->
                    noticiasRepository.marcarComoConsultada(doc.id)
                }

                RespuestaAsistente(
                    textoRespuesta = texto,
                    documentosReferenciados = documentos.take(5),
                    tipoRespuesta = TipoRespuesta.INFORMATIVA
                )
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al procesar consulta: ${e.message}", e))
        }
    }

    /**
     * Detecta la intención de la consulta (sin IA costosa)
     */
    private fun detectarIntencion(consulta: String): IntencionConsulta {
        val consultaLower = consulta.lowercase()

        return when {
            // Resumen del día
            "qué hay" in consultaLower || "resumen" in consultaLower ||
                    "noticias de hoy" in consultaLower || "últimas noticias" in consultaLower ->
                IntencionConsulta.ResumenDia

            // Búsqueda por categoría
            "deportes" in consultaLower || "deporte" in consultaLower ->
                IntencionConsulta.BuscarCategoria(Categorias.DEPORTES)

            "política" in consultaLower || "político" in consultaLower ->
                IntencionConsulta.BuscarCategoria(Categorias.POLITICA)

            "economía" in consultaLower || "económico" in consultaLower ->
                IntencionConsulta.BuscarCategoria(Categorias.ECONOMIA)

            "tecnología" in consultaLower || "tech" in consultaLower ->
                IntencionConsulta.BuscarCategoria(Categorias.TECNOLOGIA)

            "cti" in consultaLower || "concytec" in consultaLower ||
                    "investigación" in consultaLower ->
                IntencionConsulta.BuscarCategoria(Categorias.CTI)

            // Configuraciones
            "configura" in consultaLower && ("lunes" in consultaLower ||
                    "martes" in consultaLower || "miércoles" in consultaLower ||
                    "jueves" in consultaLower || "viernes" in consultaLower) -> {
                val dia = extraerDia(consultaLower)
                val categorias = extraerCategorias(consultaLower)
                IntencionConsulta.ConfigurarDia(dia, categorias)
            }

            // Noticias guardadas
            "guardadas" in consultaLower || "favoritas" in consultaLower ||
                    "mis noticias" in consultaLower ->
                IntencionConsulta.NoticiasGuardadas

            // Actualizar
            "actualiza" in consultaLower || "refresca" in consultaLower ||
                    "nuevas noticias" in consultaLower ->
                IntencionConsulta.ActualizarFuentes

            // Búsqueda libre (personas, temas específicos)
            else -> IntencionConsulta.BuscarTexto(consulta)
        }
    }

    /**
     * Busca documentos relevantes según la intención
     */
    private suspend fun buscarDocumentosRelevantes(
        intencion: IntencionConsulta,
        consulta: String
    ): List<DocumentoCTI> {
        return when (intencion) {
            is IntencionConsulta.ResumenDia -> {
                // Top 10 noticias más recientes
                noticiasRepository.obtenerRecientes(24).take(10)
            }

            is IntencionConsulta.BuscarCategoria -> {
                // Noticias de la categoría
                noticiasRepository.buscarPorTexto(intencion.categoria, limite = 10)
            }

            is IntencionConsulta.BuscarTexto -> {
                // Búsqueda por texto
                noticiasRepository.buscarPorTexto(consulta, limite = 10)
            }

            is IntencionConsulta.NoticiasGuardadas -> {
                // Devuelve vacío, se maneja diferente
                emptyList()
            }

            is IntencionConsulta.ActualizarFuentes -> {
                // Devuelve vacío, es una acción
                emptyList()
            }

            else -> emptyList()
        }
    }

    /**
     * Construye el contexto para enviar a la IA
     */
    private suspend fun construirContexto(
        documentos: List<DocumentoCTI>,
        consulta: String,
        conversacion: ContextoConversacion
    ): String {
        val config = configuracionRepository.obtenerConfiguracion()
        val categoriasActivas = configuracionRepository.obtenerCategoriasActivasHoy()

        return buildString {
            append("CATEGORÍAS DISPONIBLES:\n")
            append(Categorias.TODAS.joinToString(", "))
            append("\n\n")

            append("CATEGORÍAS ACTIVAS HOY:\n")
            append(categoriasActivas.joinToString(", "))
            append("\n\n")

            if (documentos.isNotEmpty()) {
                append("NOTICIAS ENCONTRADAS (${documentos.size}):\n\n")

                documentos.take(5).forEachIndexed { index, doc ->
                    append("--- NOTICIA ${index + 1} ---\n")
                    append(doc.obtenerContextoParaIA())
                    append("\n")
                }

                if (documentos.size > 5) {
                    append("\n(Y ${documentos.size - 5} noticias más)\n")
                }
            }

            // Historial reciente
            if (conversacion.historialMensajes.isNotEmpty()) {
                append("\nHISTORIAL RECIENTE:\n")
                conversacion.historialMensajes.takeLast(3).forEach { msg ->
                    val rol = if (msg.esUsuario) "Usuario" else "Asistente"
                    append("$rol: ${msg.texto}\n")
                }
            }
        }
    }

    /**
     * Extrae el día de la semana de un texto
     */
    private fun extraerDia(texto: String): DiaSemana {
        return when {
            "lunes" in texto -> DiaSemana.LUNES
            "martes" in texto -> DiaSemana.MARTES
            "miércoles" in texto || "miercoles" in texto -> DiaSemana.MIERCOLES
            "jueves" in texto -> DiaSemana.JUEVES
            "viernes" in texto -> DiaSemana.VIERNES
            "sábado" in texto || "sabado" in texto -> DiaSemana.SABADO
            "domingo" in texto -> DiaSemana.DOMINGO
            else -> DiaSemana.obtenerDiaActual()
        }
    }

    /**
     * Extrae categorías mencionadas en el texto
     */
    private fun extraerCategorias(texto: String): List<String> {
        val categorias = mutableListOf<String>()

        if ("deporte" in texto) categorias.add(Categorias.DEPORTES)
        if ("política" in texto || "politica" in texto) categorias.add(Categorias.POLITICA)
        if ("economía" in texto || "economia" in texto) categorias.add(Categorias.ECONOMIA)
        if ("tecnología" in texto || "tecnologia" in texto) categorias.add(Categorias.TECNOLOGIA)
        if ("entretenimiento" in texto || "espectáculo" in texto) categorias.add(Categorias.ENTRETENIMIENTO)

        return categorias.ifEmpty { listOf(Categorias.GENERAL) }
    }
}