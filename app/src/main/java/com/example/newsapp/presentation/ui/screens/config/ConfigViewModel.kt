// ==========================================
// 📄 ARCHIVO: ConfigViewModel.kt
// 📁 UBICACIÓN: presentation/ui/screens/config/
// 🟢 TIPO: Class (ViewModel)
// ==========================================

package com.example.newsapp.presentation.ui.screens.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.preferences.UserPreferencesManager
import com.example.newsapp.data.remote.ai.GeminiService
import com.example.newsapp.domain.model.ConfiguracionUsuario
import com.example.newsapp.domain.model.DiaSemana
import com.example.newsapp.domain.repository.EstadisticasNoticias
import com.example.newsapp.domain.usecase.ConfigurarPreferenciasUseCase
import com.example.newsapp.domain.usecase.LimpiarNoticiasAntiguasUseCase
import com.example.newsapp.domain.usecase.ObtenerEstadisticasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de configuración
 *
 * Maneja:
 * - Configuración de API keys
 * - Preferencias del usuario
 * - Estadísticas de uso
 * - Limpieza de datos
 */
@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val userPreferences: UserPreferencesManager,
    private val geminiService: GeminiService,
    private val configurarPreferenciasUseCase: ConfigurarPreferenciasUseCase,
    private val obtenerEstadisticasUseCase: ObtenerEstadisticasUseCase,
    private val limpiarNoticiasUseCase: LimpiarNoticiasAntiguasUseCase
) : ViewModel() {

    // ===== ESTADO DE LA UI =====

    private val _uiState = MutableStateFlow<ConfigUiState>(ConfigUiState.Idle)
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    // API Key de Gemini
    val geminiApiKey: StateFlow<String> = userPreferences.geminiApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Configuración del usuario
    private val _configuracion = MutableStateFlow<ConfiguracionUsuario?>(null)
    val configuracion: StateFlow<ConfiguracionUsuario?> = _configuracion.asStateFlow()

    // Estadísticas
    private val _estadisticas = MutableStateFlow(EstadisticasNoticias())
    val estadisticas: StateFlow<EstadisticasNoticias> = _estadisticas.asStateFlow()

    // Velocidad de voz
    val velocidadVoz: StateFlow<Float> = userPreferences.velocidadVoz
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    // Modo voz activo
    val modoVozActivo: StateFlow<Boolean> = userPreferences.modoVozActivo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        cargarDatos()
    }

    // ===== FUNCIONES PRINCIPALES =====

    /**
     * Carga los datos iniciales
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            _configuracion.value = configurarPreferenciasUseCase.obtenerConfiguracion()
            _estadisticas.value = obtenerEstadisticasUseCase()
        }
    }

    /**
     * Guarda la API key de Gemini
     */
    fun guardarGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            if (apiKey.isBlank()) {
                _uiState.value = ConfigUiState.Error("La API key no puede estar vacía")
                return@launch
            }

            try {
                userPreferences.setGeminiApiKey(apiKey)
                geminiService.configurarApiKey(apiKey)

                _uiState.value = ConfigUiState.ApiKeyGuardada

                kotlinx.coroutines.delay(2000)
                _uiState.value = ConfigUiState.Idle
            } catch (e: Exception) {
                _uiState.value = ConfigUiState.Error(
                    "Error al guardar API key: ${e.message}"
                )
            }
        }
    }

    /**
     * Configura preferencias para un día específico
     */
    fun configurarDia(dia: DiaSemana, categorias: List<String>, modoExclusivo: Boolean) {
        viewModelScope.launch {
            configurarPreferenciasUseCase.configurarDia(dia, categorias, modoExclusivo)
                .fold(
                    onSuccess = {
                        _uiState.value = ConfigUiState.ConfiguracionGuardada(
                            "Configuración para ${dia.nombreCompleto} guardada"
                        )

                        // Recarga configuración
                        _configuracion.value = configurarPreferenciasUseCase.obtenerConfiguracion()

                        kotlinx.coroutines.delay(2000)
                        _uiState.value = ConfigUiState.Idle
                    },
                    onFailure = { error ->
                        _uiState.value = ConfigUiState.Error(
                            error.message ?: "Error al guardar configuración"
                        )
                    }
                )
        }
    }

    /**
     * Excluye una categoría
     */
    fun excluirCategoria(categoria: String) {
        viewModelScope.launch {
            configurarPreferenciasUseCase.excluirCategoria(categoria)
                .fold(
                    onSuccess = {
                        _uiState.value = ConfigUiState.ConfiguracionGuardada(
                            "Categoría '$categoria' excluida"
                        )

                        _configuracion.value = configurarPreferenciasUseCase.obtenerConfiguracion()

                        kotlinx.coroutines.delay(2000)
                        _uiState.value = ConfigUiState.Idle
                    },
                    onFailure = { error ->
                        _uiState.value = ConfigUiState.Error(
                            error.message ?: "Error al excluir categoría"
                        )
                    }
                )
        }
    }

    /**
     * Agrega una keyword para seguimiento
     */
    fun seguirKeyword(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                _uiState.value = ConfigUiState.Error("La keyword no puede estar vacía")
                return@launch
            }

            configurarPreferenciasUseCase.seguirKeyword(keyword)
                .fold(
                    onSuccess = {
                        _uiState.value = ConfigUiState.ConfiguracionGuardada(
                            "Seguirás la keyword '$keyword'"
                        )

                        _configuracion.value = configurarPreferenciasUseCase.obtenerConfiguracion()

                        kotlinx.coroutines.delay(2000)
                        _uiState.value = ConfigUiState.Idle
                    },
                    onFailure = { error ->
                        _uiState.value = ConfigUiState.Error(
                            error.message ?: "Error al agregar keyword"
                        )
                    }
                )
        }
    }

    /**
     * Limpia noticias antiguas (más de 24 horas)
     */
    fun limpiarNoticiasAntiguas() {
        viewModelScope.launch {
            _uiState.value = ConfigUiState.Limpiando

            limpiarNoticiasUseCase().fold(
                onSuccess = { cantidad ->
                    _uiState.value = ConfigUiState.LimpiezaCompletada(
                        "$cantidad noticias eliminadas"
                    )

                    // Actualiza estadísticas
                    _estadisticas.value = obtenerEstadisticasUseCase()

                    kotlinx.coroutines.delay(2000)
                    _uiState.value = ConfigUiState.Idle
                },
                onFailure = { error ->
                    _uiState.value = ConfigUiState.Error(
                        error.message ?: "Error al limpiar noticias"
                    )

                    kotlinx.coroutines.delay(3000)
                    _uiState.value = ConfigUiState.Idle
                }
            )
        }
    }

    /**
     * Configura la velocidad de voz
     */
    fun setVelocidadVoz(velocidad: Float) {
        viewModelScope.launch {
            userPreferences.setVelocidadVoz(velocidad)
        }
    }

    /**
     * Activa/desactiva el modo voz
     */
    fun setModoVozActivo(activo: Boolean) {
        viewModelScope.launch {
            userPreferences.setModoVozActivo(activo)
        }
    }

    /**
     * Verifica si Gemini está configurado
     */
    fun geminiEstaConfigurado(): Boolean {
        return geminiService.estaConfigurado()
    }

    /**
     * Resetea el estado
     */
    fun resetearEstado() {
        _uiState.value = ConfigUiState.Idle
    }
}

// ===== ESTADOS DE LA UI =====

/**
 * Estados de la UI de Configuración
 */
sealed class ConfigUiState {
    object Idle : ConfigUiState()
    object ApiKeyGuardada : ConfigUiState()
    data class ConfiguracionGuardada(val mensaje: String) : ConfigUiState()
    object Limpiando : ConfigUiState()
    data class LimpiezaCompletada(val mensaje: String) : ConfigUiState()
    data class Error(val mensaje: String) : ConfigUiState()
}

// ==========================================
// FIN DE ARCHIVO ConfigViewModel.kt
// ==========================================