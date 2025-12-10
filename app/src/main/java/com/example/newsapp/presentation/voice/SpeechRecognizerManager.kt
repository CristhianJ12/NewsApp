package com.example.newsapp.presentation.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para Speech Recognition (Conversión de voz a texto)
 * Usa el reconocedor nativo de Android (GRATIS)
 *
 * COSTO: $0
 */
@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var _estaEscuchando = false

    val estaEscuchando: Boolean
        get() = _estaEscuchando

    /**
     * Verifica si el reconocimiento de voz está disponible
     */
    fun estaDisponible(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Escucha y reconoce voz
     * Retorna un Flow que emite el progreso y resultado
     */
    fun escuchar(): Flow<EstadoReconocimiento> = callbackFlow {
        if (!estaDisponible()) {
            trySend(EstadoReconocimiento.Error("Reconocimiento de voz no disponible"))
            close()
            return@callbackFlow
        }

        // Crea el recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        // Configura el intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("es", "PE").toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-PE")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-PE")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Timeouts
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
        }

        // Configura el listener
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _estaEscuchando = true
                trySend(EstadoReconocimiento.Listo)
            }

            override fun onBeginningOfSpeech() {
                trySend(EstadoReconocimiento.Escuchando)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Nivel de volumen (útil para UI)
                trySend(EstadoReconocimiento.Volumen(rmsdB))
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer de audio recibido
            }

            override fun onEndOfSpeech() {
                _estaEscuchando = false
                trySend(EstadoReconocimiento.Procesando)
            }

            override fun onError(error: Int) {
                _estaEscuchando = false

                val mensaje = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Permiso de micrófono no concedido"
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Tiempo de espera de red agotado"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        "No se reconoció ninguna voz. Intenta hablar más claro"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        "Reconocedor ocupado, intenta de nuevo"
                    SpeechRecognizer.ERROR_SERVER -> "Error del servidor de Google"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "No se detectó voz. Por favor habla"
                    else -> "Error desconocido: $error"
                }

                trySend(EstadoReconocimiento.Error(mensaje))
                close()
                limpiar()
            }

            override fun onResults(results: Bundle?) {
                _estaEscuchando = false

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                if (matches.isNullOrEmpty()) {
                    trySend(EstadoReconocimiento.Error("No se reconoció ninguna voz"))
                } else {
                    // Toma el resultado con mayor confianza
                    val textoReconocido = matches[0]
                    val confianza = confidences?.getOrNull(0) ?: 0f

                    trySend(
                        EstadoReconocimiento.Resultado(
                            texto = textoReconocido,
                            confianza = confianza,
                            alternativas = matches.drop(1)
                        )
                    )
                }

                close()
                limpiar()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Resultados parciales mientras habla
                val matches = partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                if (!matches.isNullOrEmpty()) {
                    trySend(EstadoReconocimiento.Parcial(matches[0]))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Eventos adicionales
            }
        })

        // Inicia el reconocimiento
        trySend(EstadoReconocimiento.Iniciando)
        speechRecognizer?.startListening(intent)

        awaitClose {
            cancelar()
        }
    }

    /**
     * Cancela el reconocimiento actual
     */
    fun cancelar() {
        speechRecognizer?.cancel()
        _estaEscuchando = false
        limpiar()
    }

    /**
     * Detiene el reconocimiento actual
     */
    fun detener() {
        speechRecognizer?.stopListening()
        _estaEscuchando = false
    }

    /**
     * Limpia recursos
     */
    private fun limpiar() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _estaEscuchando = false
    }
}

/**
 * Estados del reconocimiento de voz
 */
sealed class EstadoReconocimiento {
    object Iniciando : EstadoReconocimiento()
    object Listo : EstadoReconocimiento()
    object Escuchando : EstadoReconocimiento()
    object Procesando : EstadoReconocimiento()
    data class Volumen(val nivel: Float) : EstadoReconocimiento()
    data class Parcial(val texto: String) : EstadoReconocimiento()
    data class Resultado(
        val texto: String,
        val confianza: Float,
        val alternativas: List<String> = emptyList()
    ) : EstadoReconocimiento()
    data class Error(val mensaje: String) : EstadoReconocimiento()
}