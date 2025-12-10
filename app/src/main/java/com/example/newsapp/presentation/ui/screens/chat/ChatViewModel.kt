package com.example.newsapp.presentation.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.domain.model.ContextoConversacion
import com.example.newsapp.domain.model.MensajeChat
import com.example.newsapp.domain.model.RespuestaAsistente
import com.example.newsapp.domain.model.TipoRespuesta
import com.example.newsapp.domain.usecase.AsistenteConversacionalUseCase
import com.example.newsapp.presentation.voice.EstadoReconocimiento
import com.example.newsapp.presentation.voice.EstadoTTS
import com.example.newsapp.presentation.voice.SpeechRecognizerManager
import com.example.newsapp.presentation.voice.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de chat conversacional
 *
 * Este es el cerebro de la interacción por voz.
 * Coordina el reconocimiento de voz, procesamiento con IA y respuesta por voz.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val asistenteUseCase: AsistenteConversacionalUseCase,
    private val ttsManager: TTSManager,
    private val speechManager: SpeechRecognizerManager
) : ViewModel() {

    // ===== ESTADO DE LA UI =====

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _mensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val mensajes: StateFlow<List<MensajeChat>> = _mensajes.asStateFlow()

    private val _estadoVoz = MutableStateFlow<EstadoVoz>(EstadoVoz.Inactivo)
    val estadoVoz: StateFlow<EstadoVoz> = _estadoVoz.asStateFlow()

    // Contexto de conversación (historial)
    private var contexto = ContextoConversacion()

    init {
        // Inicializa TTS al crear el ViewModel
        viewModelScope.launch {
            ttsManager.inicializar()
        }

        // Mensaje de bienvenida
        agregarMensaje(
            MensajeChat(
                texto = "¡Hola! Soy tu asistente de noticias. " +
                        "Puedes preguntarme sobre las noticias de hoy o pedirme que configure tus preferencias. " +
                        "¿En qué te puedo ayudar?",
                esUsuario = false
            )
        )
    }

    // ===== FUNCIONES PRINCIPALES =====

    /**
     * Inicia el reconocimiento de voz
     */
    fun iniciarReconocimientoVoz() {
        if (!speechManager.estaDisponible()) {
            _uiState.value = ChatUiState.Error("Reconocimiento de voz no disponible")
            return
        }

        viewModelScope.launch {
            speechManager.escuchar().collect { estado ->
                when (estado) {
                    is EstadoReconocimiento.Iniciando -> {
                        _estadoVoz.value = EstadoVoz.Iniciando
                    }

                    is EstadoReconocimiento.Listo -> {
                        _estadoVoz.value = EstadoVoz.Escuchando
                    }

                    is EstadoReconocimiento.Escuchando -> {
                        _estadoVoz.value = EstadoVoz.Escuchando
                    }

                    is EstadoReconocimiento.Parcial -> {
                        _estadoVoz.value = EstadoVoz.EscuchandoParcial(estado.texto)
                    }

                    is EstadoReconocimiento.Procesando -> {
                        _estadoVoz.value = EstadoVoz.Procesando
                    }

                    is EstadoReconocimiento.Resultado -> {
                        _estadoVoz.value = EstadoVoz.Inactivo
                        procesarConsultaUsuario(estado.texto)
                    }

                    is EstadoReconocimiento.Error -> {
                        _estadoVoz.value = EstadoVoz.Inactivo
                        _uiState.value = ChatUiState.Error(estado.mensaje)
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * Cancela el reconocimiento de voz
     */
    fun cancelarReconocimientoVoz() {
        speechManager.cancelar()
        _estadoVoz.value = EstadoVoz.Inactivo
    }

    /**
     * Envía una consulta por texto (sin voz)
     */
    fun enviarConsultaTexto(texto: String) {
        if (texto.isBlank()) return
        procesarConsultaUsuario(texto)
    }

    /**
     * Procesa la consulta del usuario (por voz o texto)
     */
    private fun procesarConsultaUsuario(consulta: String) {
        viewModelScope.launch {
            // Agrega mensaje del usuario
            val mensajeUsuario = MensajeChat(
                texto = consulta,
                esUsuario = true
            )
            agregarMensaje(mensajeUsuario)
            contexto = contexto.agregarMensaje(mensajeUsuario)

            // Muestra estado de carga
            _uiState.value = ChatUiState.Loading

            // Procesa con el asistente
            asistenteUseCase(consulta, contexto).fold(
                onSuccess = { respuesta ->
                    manejarRespuestaAsistente(respuesta)
                },
                onFailure = { error ->
                    _uiState.value = ChatUiState.Error(
                        error.message ?: "Error al procesar consulta"
                    )
                }
            )
        }
    }

    /**
     * Maneja la respuesta del asistente
     */
    private fun manejarRespuestaAsistente(respuesta: RespuestaAsistente) {
        // Agrega mensaje del asistente
        val mensajeAsistente = MensajeChat(
            texto = respuesta.textoRespuesta,
            esUsuario = false,
            documentosReferenciados = respuesta.documentosReferenciados.map { it.id }
        )
        agregarMensaje(mensajeAsistente)
        contexto = contexto.agregarMensaje(mensajeAsistente)

        // Actualiza UI según el tipo de respuesta
        _uiState.value = when (respuesta.tipoRespuesta) {
            TipoRespuesta.INFORMATIVA -> ChatUiState.Success
            TipoRespuesta.CONFIGURACION_EXITOSA -> ChatUiState.ConfiguracionExitosa
            TipoRespuesta.CONSULTA_VACIA -> ChatUiState.SinResultados
            TipoRespuesta.ERROR -> ChatUiState.Error(respuesta.textoRespuesta)
            else -> ChatUiState.Success
        }

        // Lee la respuesta en voz alta
        leerRespuestaEnVoz(respuesta.textoRespuesta)
    }

    /**
     * Lee un texto en voz alta
     */
    fun leerRespuestaEnVoz(texto: String) {
        viewModelScope.launch {
            ttsManager.hablar(texto).collect { estado ->
                when (estado) {
                    is EstadoTTS.Iniciando -> {
                        _estadoVoz.value = EstadoVoz.Hablando
                    }

                    is EstadoTTS.Hablando -> {
                        _estadoVoz.value = EstadoVoz.Hablando
                    }

                    is EstadoTTS.Completado -> {
                        _estadoVoz.value = EstadoVoz.Inactivo
                    }

                    is EstadoTTS.Error -> {
                        _estadoVoz.value = EstadoVoz.Inactivo
                        _uiState.value = ChatUiState.Error(estado.mensaje)
                    }
                }
            }
        }
    }

    /**
     * Detiene la lectura de voz
     */
    fun detenerVoz() {
        ttsManager.detener()
        _estadoVoz.value = EstadoVoz.Inactivo
    }

    /**
     * Limpia el historial de mensajes
     */
    fun limpiarHistorial() {
        _mensajes.value = emptyList()
        contexto = ContextoConversacion()
        _uiState.value = ChatUiState.Idle

        // Agrega mensaje de bienvenida nuevamente
        agregarMensaje(
            MensajeChat(
                texto = "Historial limpiado. ¿En qué te puedo ayudar?",
                esUsuario = false
            )
        )
    }

    /**
     * Agrega un mensaje al historial
     */
    private fun agregarMensaje(mensaje: MensajeChat) {
        _mensajes.value = _mensajes.value + mensaje
    }

    /**
     * Resetea el estado de error
     */
    fun resetearEstado() {
        _uiState.value = ChatUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.liberar()
    }
}

// ===== ESTADOS DE LA UI =====

/**
 * Estados de la UI del chat
 */
sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()
    object Success : ChatUiState()
    object ConfiguracionExitosa : ChatUiState()
    object SinResultados : ChatUiState()
    data class Error(val mensaje: String) : ChatUiState()
}

/**
 * Estados de la voz (TTS + Speech)
 */
sealed class EstadoVoz {
    object Inactivo : EstadoVoz()
    object Iniciando : EstadoVoz()
    object Escuchando : EstadoVoz()
    data class EscuchandoParcial(val textoParcial: String) : EstadoVoz()
    object Procesando : EstadoVoz()
    object Hablando : EstadoVoz()
}