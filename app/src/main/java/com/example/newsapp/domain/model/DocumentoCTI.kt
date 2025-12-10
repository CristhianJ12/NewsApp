package com.example.newsapp.domain.model

import kotlinx.serialization.Serializable

/**
 * Modelo de dominio para un documento/noticia CTI
 * Este es el modelo que se usa en toda la app
 */
@Serializable
data class DocumentoCTI(
    val id: String,
    val titulo: String,
    val contenidoCompleto: String,
    val resumenEjecutivo: String? = null,

    // Metadata especializada
    val categoria: String,
    val diario: String,
    val entidadResponsable: String? = null,
    val palabrasClave: List<String> = emptyList(),

    // Temporalidad
    val fechaPublicacion: Long,
    val fechaIngesta: Long = System.currentTimeMillis(),

    // Estado
    val urlOriginal: String,
    val fueConsultado: Boolean = false,
    val vecesConsultado: Int = 0,
    val esGuardado: Boolean = false
) {
    /**
     * Verifica si es de las últimas 24 horas
     */
    fun esReciente(): Boolean {
        val hace24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return fechaPublicacion >= hace24h
    }

    /**
     * Calcula un score simple de relevancia para búsqueda
     */
    fun calcularScore(consulta: String): Int {
        var score = 0
        val consultaLower = consulta.lowercase()
        val tituloLower = titulo.lowercase()

        // Coincidencia en título
        if (consultaLower in tituloLower) score += 50

        // Coincidencia en palabras clave
        palabrasClave.forEach { keyword ->
            if (keyword.lowercase() in consultaLower) score += 20
        }

        // Recencia (más reciente = más score)
        if (esReciente()) score += 30

        // Penalizar si ya fue consultado
        if (fueConsultado) score -= 10

        return score
    }

    /**
     * Texto para contexto de IA (limitado para reducir tokens)
     */
    fun obtenerContextoParaIA(): String {
        return buildString {
            append("TÍTULO: $titulo\n")
            append("DIARIO: $diario\n")
            append("CATEGORÍA: $categoria\n")
            if (entidadResponsable != null) {
                append("ENTIDAD: $entidadResponsable\n")
            }
            append("FECHA: ${formatearFecha(fechaPublicacion)}\n")
            append("CONTENIDO: ${contenidoCompleto.take(1500)}...\n")
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("es", "PE"))
        return sdf.format(java.util.Date(timestamp))
    }
}

/**
 * Categorías disponibles en el sistema
 */
object Categorias {
    const val POLITICA = "Política"
    const val ECONOMIA = "Economía"
    const val DEPORTES = "Deportes"
    const val TECNOLOGIA = "Tecnología"
    const val CTI = "CTI"
    const val SALUD = "Salud"
    const val ENTRETENIMIENTO = "Entretenimiento"
    const val GENERAL = "General"

    val TODAS = listOf(
        POLITICA,
        ECONOMIA,
        DEPORTES,
        TECNOLOGIA,
        CTI,
        SALUD,
        ENTRETENIMIENTO,
        GENERAL
    )

    /**
     * Clasifica automáticamente por keywords
     */
    fun clasificarPorContenido(titulo: String, contenido: String): String {
        val texto = "$titulo $contenido".lowercase()

        return when {
            // CTI - Prioridad alta
            "concytec" in texto || "investigación" in texto ||
                    "innovación" in texto || "tecnología" in texto -> CTI

            // Política
            "congreso" in texto || "ministro" in texto ||
                    "presidente" in texto || "gobierno" in texto -> POLITICA

            // Deportes
            "fútbol" in texto || "deporte" in texto ||
                    "campeón" in texto || "liga" in texto -> DEPORTES

            // Economía
            "dólar" in texto || "economía" in texto ||
                    "mercado" in texto || "banco" in texto -> ECONOMIA

            // Tecnología
            "app" in texto || "software" in texto ||
                    "inteligencia artificial" in texto -> TECNOLOGIA

            // Salud
            "salud" in texto || "hospital" in texto ||
                    "medicina" in texto -> SALUD

            // Entretenimiento
            "película" in texto || "música" in texto ||
                    "artista" in texto || "concierto" in texto -> ENTRETENIMIENTO

            else -> GENERAL
        }
    }

    /**
     * Extrae keywords importantes del texto
     */
    fun extraerKeywords(titulo: String, contenido: String): List<String> {
        val texto = "$titulo $contenido".lowercase()
        val keywords = mutableListOf<String>()

        // Keywords importantes para CTI
        val keywordsCTI = listOf(
            "concytec", "pct", "innovación", "investigación",
            "ciencia", "tecnología", "patente"
        )

        // Keywords de entidades
        val entidades = listOf(
            "congreso", "ministro", "indeci", "sunat",
            "gobierno", "municipalidad"
        )

        // Keywords deportivos
        val deportes = listOf(
            "cueva", "lapadula", "guerrero", "carrillo",
            "universitario", "alianza lima", "cristal"
        )

        // Buscar coincidencias
        (keywordsCTI + entidades + deportes).forEach { keyword ->
            if (keyword in texto) {
                keywords.add(keyword.replaceFirstChar { it.uppercase() })
            }
        }

        return keywords.distinct()
    }
}