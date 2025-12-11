package com.example.newsapp.data.remote.sources

import com.example.newsapp.domain.model.Categorias
import com.example.newsapp.domain.model.DocumentoCTI
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de noticias RSS
 * Obtiene noticias de diferentes diarios peruanos
 *
 * COSTO: $0 (solo consume datos de internet)
 */
@Singleton
class RSSSource @Inject constructor(
    private val rssParser: RssParser
) {

    /**
     * Fuentes RSS configuradas
     * Puedes agregar/quitar fuentes aquí
     */
    private val fuentes = listOf(
        FuenteRSS(
            nombre = "RPP Noticias",
            url = "https://rpp.pe/feed",
            activa = true
        ),
        FuenteRSS(
            nombre = "El Comercio",
            url = "https://elcomercio.pe/arcio/rss/",
            activa = true
        ),
        FuenteRSS(
            nombre = "Gestión",
            url = "https://gestion.pe/feed/",
            activa = true
        ),
        FuenteRSS(
            nombre = "La República",
            url = "https://larepublica.pe/rss",
            activa = true
        ),
        FuenteRSS(
            nombre = "Perú21",
            url = "https://peru21.pe/feed/",
            activa = true
        )
    )

    /**
     * Obtiene noticias de todas las fuentes RSS activas
     * Se ejecuta en paralelo para mayor velocidad
     */
    suspend fun obtenerNoticias(): Result<List<DocumentoCTI>> = withContext(Dispatchers.IO) {
        try {
            val noticiasDeTodasLasFuentes = mutableListOf<DocumentoCTI>()

            // Procesa todas las fuentes en paralelo
            val resultados = fuentes
                .filter { it.activa }
                .map { fuente ->
                    async {
                        try {
                            obtenerDeUnaFuente(fuente)
                        } catch (e: Exception) {
                            println("Error al obtener de ${fuente.nombre}: ${e.message}")
                            emptyList<DocumentoCTI>()
                        }
                    }
                }
                .awaitAll()

            // Combina todos los resultados
            resultados.forEach { noticias ->
                noticiasDeTodasLasFuentes.addAll(noticias)
            }

            if (noticiasDeTodasLasFuentes.isEmpty()) {
                Result.failure(Exception("No se pudieron obtener noticias de ninguna fuente"))
            } else {
                Result.success(noticiasDeTodasLasFuentes)
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error general al obtener noticias: ${e.message}", e))
        }
    }

    /**
     * Obtiene noticias de una fuente específica
     */
    private suspend fun obtenerDeUnaFuente(fuente: FuenteRSS): List<DocumentoCTI> {
        return try {
            val channel = rssParser.getRssChannel(fuente.url)
            parsearChannel(channel, fuente)
        } catch (e: Exception) {
            println("Error parseando ${fuente.nombre}: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parsea un canal RSS a lista de DocumentoCTI
     */
    private fun parsearChannel(channel: RssChannel, fuente: FuenteRSS): List<DocumentoCTI> {
        return channel.items.mapNotNull { item ->
            try {
                val titulo = item.title?.trim() ?: return@mapNotNull null
                val contenido = item.description?.let { limpiarHtml(it) } ?: titulo
                val url = item.link ?: return@mapNotNull null

                // Genera ID único
                val id = generarId(titulo, url)

                // Clasifica automáticamente
                val categoria = Categorias.clasificarPorContenido(titulo, contenido)

                // Extrae keywords
                val keywords = Categorias.extraerKeywords(titulo, contenido)

                // Detecta entidad responsable
                val entidad = detectarEntidad(titulo, contenido)

                // CORRECCIÓN: Parsea la fecha correctamente
                val fechaPublicacion = item.pubDate?.let { fechaString ->
                    parsearFecha(fechaString)
                } ?: System.currentTimeMillis()

                DocumentoCTI(
                    id = id,
                    titulo = titulo,
                    contenidoCompleto = contenido,
                    categoria = categoria,
                    diario = fuente.nombre,
                    entidadResponsable = entidad,
                    palabrasClave = keywords,
                    fechaPublicacion = fechaPublicacion,
                    urlOriginal = url
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Parsea una fecha en formato String a Long (milisegundos)
     * Prueba con varios formatos comunes en RSS
     */
    private fun parsearFecha(fechaString: String): Long {
        val formatos = listOf(
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        )

        for (formato in formatos) {
            try {
                val fecha = formato.parse(fechaString)
                if (fecha != null) {
                    return fecha.time
                }
            } catch (e: Exception) {
                // Intenta con el siguiente formato
            }
        }

        // Si ningún formato funciona, retorna la fecha actual
        return System.currentTimeMillis()
    }

    /**
     * Limpia tags HTML del contenido
     */
    private fun limpiarHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), "") // Remueve tags HTML
            .replace(Regex("&[a-z]+;"), " ") // Remueve entidades HTML
            .replace(Regex("\\s+"), " ") // Normaliza espacios
            .replace("&nbsp;", " ")
            .trim()
    }

    /**
     * Genera un ID único basado en título y URL
     */
    private fun generarId(titulo: String, url: String): String {
        val input = "$titulo$url"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Detecta la entidad responsable en el texto
     */
    private fun detectarEntidad(titulo: String, contenido: String): String? {
        val texto = "$titulo $contenido".lowercase()

        val entidades = mapOf(
            "concytec" to "CONCYTEC",
            "congreso" to "Congreso de la República",
            "indeci" to "INDECI",
            "sunat" to "SUNAT",
            "ministerio" to "Gobierno",
            "pcm" to "PCM",
            "minedu" to "MINEDU",
            "minsa" to "MINSA",
            "produce" to "PRODUCE"
        )

        return entidades.entries.firstOrNull { (keyword, _) ->
            keyword in texto
        }?.value
    }

    /**
     * Obtiene noticias de una categoría específica
     */
    suspend fun obtenerPorCategoria(categoria: String): Result<List<DocumentoCTI>> {
        return obtenerNoticias().map { noticias ->
            noticias.filter { it.categoria == categoria }
        }
    }
}

/**
 * Datos de una fuente RSS
 */
data class FuenteRSS(
    val nombre: String,
    val url: String,
    val activa: Boolean = true
)