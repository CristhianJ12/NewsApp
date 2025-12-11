package com.example.newsapp.domain.model

/**
 * Configuración de preferencias del usuario
 * Se guarda persistentemente en BD
 *
 * NOTA: No usa @Serializable porque Room maneja la serialización
 * a través de Gson en los Converters
 */
data class ConfiguracionUsuario(
    val id: String = "default",
    val preferenciasSemanales: Map<DiaSemana, PreferenciaDia> = emptyMap(),
    val categoriasExcluidas: List<String> = emptyList(),
    val diariosPreferidos: List<String> = emptyList(),
    val keywordsSeguimiento: List<String> = emptyList(),
    val alertasActivas: Boolean = true,
    val horaConsolidacionMañana: String = "08:00",
    val horaConsolidacionTarde: String = "17:00"
)

/**
 * Días de la semana
 */
enum class DiaSemana(val nombreCompleto: String) {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    companion object {
        fun obtenerDiaActual(): DiaSemana {
            val calendar = java.util.Calendar.getInstance()
            return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> LUNES
                java.util.Calendar.TUESDAY -> MARTES
                java.util.Calendar.WEDNESDAY -> MIERCOLES
                java.util.Calendar.THURSDAY -> JUEVES
                java.util.Calendar.FRIDAY -> VIERNES
                java.util.Calendar.SATURDAY -> SABADO
                java.util.Calendar.SUNDAY -> DOMINGO
                else -> LUNES
            }
        }

        fun fromString(dia: String): DiaSemana? {
            return entries.find {
                it.nombreCompleto.equals(dia, ignoreCase = true) ||
                        it.name.equals(dia, ignoreCase = true)
            }
        }
    }
}

/**
 * Preferencia para un día específico
 */
data class PreferenciaDia(
    val categoriasActivas: List<String> = emptyList(),
    val modoExclusivo: Boolean = false, // true = SOLO estas categorías
    val horaPreferida: String? = null
)

/**
 * Respuesta del asistente conversacional
 */
data class RespuestaAsistente(
    val textoRespuesta: String,
    val documentosReferenciados: List<DocumentoCTI> = emptyList(),
    val tipoRespuesta: TipoRespuesta = TipoRespuesta.INFORMATIVA,
    val accionSugerida: AccionSugerida? = null,
    val configuracionAplicada: ConfiguracionAplicada? = null
)

/**
 * Tipo de respuesta del asistente
 */
enum class TipoRespuesta {
    INFORMATIVA,           // Responde con información
    CONFIGURACION_EXITOSA, // Configuró algo
    CONFIGURACION_FALLIDA, // No pudo configurar
    CONSULTA_VACIA,        // No hay información
    ERROR                  // Error en el procesamiento
}

/**
 * Acciones que el asistente puede sugerir
 */
enum class AccionSugerida {
    ACTUALIZAR_FUENTES,
    LEER_DETALLE,
    GUARDAR_NOTICIA,
    CONFIGURAR_PREFERENCIAS,
    VER_MAS_CATEGORIA
}

/**
 * Configuración aplicada por el asistente
 */
data class ConfiguracionAplicada(
    val tipo: TipoConfiguracion,
    val parametros: Map<String, String> = emptyMap()
)

enum class TipoConfiguracion {
    PREFERENCIA_DIA,
    EXCLUIR_CATEGORIA,
    SEGUIR_KEYWORD,
    DIARIO_PREFERIDO,
    HORA_CONSOLIDACION
}

/**
 * Intención detectada en la consulta del usuario
 */
sealed class IntencionConsulta {
    // Consultas de información
    object ResumenDia : IntencionConsulta()
    data class BuscarCategoria(val categoria: String) : IntencionConsulta()
    data class BuscarTexto(val texto: String) : IntencionConsulta()
    data class BuscarPersona(val nombre: String) : IntencionConsulta()
    object NoticiasRecientes : IntencionConsulta()
    object NoticiasGuardadas : IntencionConsulta()

    // Configuraciones
    data class ConfigurarDia(val dia: DiaSemana, val categorias: List<String>) : IntencionConsulta()
    data class ExcluirCategoria(val categoria: String) : IntencionConsulta()
    data class SeguirKeyword(val keyword: String) : IntencionConsulta()
    data class PreferirDiario(val diario: String) : IntencionConsulta()

    // Acciones
    data class LeerNoticia(val indice: Int) : IntencionConsulta()
    data class GuardarNoticia(val id: String) : IntencionConsulta()
    object ActualizarFuentes : IntencionConsulta()

    // No reconocida
    data class NoReconocida(val textoOriginal: String) : IntencionConsulta()
}

/**
 * Contexto de conversación (para mantener estado)
 */
data class ContextoConversacion(
    val historialMensajes: List<MensajeChat> = emptyList(),
    val ultimosDocumentosMencionados: List<String> = emptyList(),
    val categoriaActual: String? = null,
    val diaReferencia: DiaSemana = DiaSemana.obtenerDiaActual()
) {
    fun agregarMensaje(mensaje: MensajeChat): ContextoConversacion {
        val nuevoHistorial = (historialMensajes + mensaje).takeLast(10) // Solo últimos 10
        return copy(historialMensajes = nuevoHistorial)
    }
}

/**
 * Mensaje en el chat
 */
data class MensajeChat(
    val id: String = java.util.UUID.randomUUID().toString(),
    val texto: String,
    val esUsuario: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val documentosReferenciados: List<String> = emptyList()
)