package com.example.newsapp.presentation.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manager para Text-to-Speech (Conversión de texto a voz)
 * Usa el TTS nativo de Android (GRATIS)
 *
 * COSTO: $0
 */
@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var tts: TextToSpeech? = null
    private var estaInicializado = false
    private var _estaHablando = false

    val estaHablando: Boolean
        get() = _estaHablando

    /**
     * Inicializa el motor TTS
     */
    suspend fun inicializar(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let { engine ->
                        // Configura español peruano (o español general)
                        val localePeruano = Locale("es", "PE")
                        val resultado = engine.setLanguage(localePeruano)

                        when (resultado) {
                            TextToSpeech.LANG_MISSING_DATA,
                            TextToSpeech.LANG_NOT_SUPPORTED -> {
                                // Intenta con español de España
                                engine.setLanguage(Locale("es", "ES"))
                            }
                        }

                        // Configuración inicial
                        engine.setSpeechRate(1.0f)  // Velocidad normal
                        engine.setPitch(1.0f)       // Tono normal

                        estaInicializado = true

                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(
                            Result.failure(Exception("Error al inicializar TTS"))
                        )
                    }
                }
            }

            // Maneja cancelación
            continuation.invokeOnCancellation {
                liberar()
            }

        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(
                    Result.failure(Exception("Error al crear TTS: ${e.message}", e))
                )
            }
        }
    }

    /**
     * Lee un texto en voz alta
     * Retorna un Flow que emite el progreso
     */
    fun hablar(texto: String): Flow<EstadoTTS> = callbackFlow {
        if (!estaInicializado || tts == null) {
            trySend(EstadoTTS.Error("TTS no inicializado"))
            close()
            return@callbackFlow
        }

        if (texto.isBlank()) {
            trySend(EstadoTTS.Error("Texto vacío"))
            close()
            return@callbackFlow
        }

        _estaHablando = true
        trySend(EstadoTTS.Iniciando)

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                trySend(EstadoTTS.Hablando)
            }

            override fun onDone(utteranceId: String?) {
                _estaHablando = false
                trySend(EstadoTTS.Completado)
                close()
            }

            override fun onError(utteranceId: String?) {
                _estaHablando = false
                trySend(EstadoTTS.Error("Error al reproducir audio"))
                close()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?, errorCode: Int) {
                _estaHablando = false
                trySend(EstadoTTS.Error("Error código: $errorCode"))
                close()
            }
        })

        // Divide texto largo en fragmentos
        val fragmentos = dividirTexto(texto)

        fragmentos.forEachIndexed { index, fragmento ->
            val queue = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(fragmento, queue, null, "utterance_$index")
        }

        awaitClose {
            detener()
        }
    }

    /**
     * Detiene la lectura actual
     */
    fun detener() {
        tts?.stop()
        _estaHablando = false
    }

    /**
     * Configura la velocidad de habla
     * @param velocidad 0.5 (lento) a 2.0 (rápido), 1.0 = normal
     */
    fun setVelocidad(velocidad: Float) {
        tts?.setSpeechRate(velocidad.coerceIn(0.5f, 2.0f))
    }

    /**
     * Configura el tono de voz
     * @param tono 0.5 (grave) a 2.0 (agudo), 1.0 = normal
     */
    fun setTono(tono: Float) {
        tts?.setPitch(tono.coerceIn(0.5f, 2.0f))
    }

    /**
     * Libera recursos del TTS
     */
    fun liberar() {
        detener()
        tts?.shutdown()
        tts = null
        estaInicializado = false
        _estaHablando = false
    }

    /**
     * Divide texto largo en fragmentos manejables
     * TTS tiene límite de ~4000 caracteres por utterance
     */
    private fun dividirTexto(texto: String, maxLength: Int = 3500): List<String> {
        if (texto.length <= maxLength) {
            return listOf(texto)
        }

        val fragmentos = mutableListOf<String>()
        var textoRestante = texto

        while (textoRestante.length > maxLength) {
            // Busca el último punto antes del límite
            var indiceCorte = textoRestante.lastIndexOf('.', maxLength)

            // Si no hay punto, busca coma
            if (indiceCorte == -1) {
                indiceCorte = textoRestante.lastIndexOf(',', maxLength)
            }

            // Si no hay coma, busca espacio
            if (indiceCorte == -1) {
                indiceCorte = textoRestante.lastIndexOf(' ', maxLength)
            }

            // Si no hay nada, corta en el límite
            if (indiceCorte == -1) {
                indiceCorte = maxLength
            }

            fragmentos.add(textoRestante.substring(0, indiceCorte + 1).trim())
            textoRestante = textoRestante.substring(indiceCorte + 1).trim()
        }

        if (textoRestante.isNotEmpty()) {
            fragmentos.add(textoRestante)
        }

        return fragmentos
    }
}

/**
 * Estados del TTS
 */
sealed class EstadoTTS {
    object Iniciando : EstadoTTS()
    object Hablando : EstadoTTS()
    object Completado : EstadoTTS()
    data class Error(val mensaje: String) : EstadoTTS()
}