// ==========================================
// 📄 ARCHIVO: HomeViewModel.kt
// 📁 UBICACIÓN: presentation/ui/screens/home/
// 🟢 TIPO: Class (ViewModel)
// ==========================================

package com.example.newsapp.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.domain.model.Categorias
import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.repository.ConfiguracionRepository
import com.example.newsapp.domain.repository.EstadisticasNoticias
import com.example.newsapp.domain.repository.NoticiasRepository
import com.example.newsapp.domain.usecase.GenerarResumenUseCase
import com.example.newsapp.domain.usecase.GuardarNoticiaUseCase
import com.example.newsapp.domain.usecase.IngestionarNoticiasUseCase
import com.example.newsapp.domain.usecase.ObtenerEstadisticasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal (Home)
 *
 * Muestra la lista de noticias y permite:
 * - Ver noticias por categoría
 * - Actualizar desde RSS
 * - Generar resúmenes
 * - Guardar favoritas
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noticiasRepository: NoticiasRepository,
    private val configuracionRepository: ConfiguracionRepository,
    private val ingestionarUseCase: IngestionarNoticiasUseCase,
    private val generarResumenUseCase: GenerarResumenUseCase,
    private val guardarNoticiaUseCase: GuardarNoticiaUseCase,
    private val obtenerEstadisticasUseCase: ObtenerEstadisticasUseCase
) : ViewModel() {

    // ===== ESTADO DE LA UI =====

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Categoría seleccionada
    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    // Todas las categorías disponibles
    val categoriasDisponibles = Categorias.TODAS

    // Noticias filtradas por categoría
    val noticias: StateFlow<List<DocumentoCTI>> = combine(
        _categoriaSeleccionada,
        noticiasRepository.observarNoticias()
    ) { categoria, todasNoticias ->
        if (categoria == null) {
            todasNoticias
        } else {
            todasNoticias.filter { it.categoria == categoria }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estadísticas
    private val _estadisticas = MutableStateFlow(EstadisticasNoticias())
    val estadisticas: StateFlow<EstadisticasNoticias> = _estadisticas.asStateFlow()

    init {
        cargarDatos()
    }

    // ===== FUNCIONES PRINCIPALES =====

    /**
     * Carga los datos iniciales
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Observa las noticias
            noticias.collect { listaNoticias ->
                if (listaNoticias.isEmpty()) {
                    _uiState.value = HomeUiState.Empty
                } else {
                    _uiState.value = HomeUiState.Success
                    actualizarEstadisticas()
                }
            }
        }
    }

    /**
     * Actualiza noticias desde las fuentes RSS
     */
    fun actualizarNoticias() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Actualizando

            ingestionarUseCase().fold(
                onSuccess = { cantidadNuevas ->
                    _uiState.value = if (cantidadNuevas > 0) {
                        HomeUiState.ActualizacionExitosa(
                            "$cantidadNuevas noticias nuevas cargadas"
                        )
                    } else {
                        HomeUiState.ActualizacionExitosa("No hay noticias nuevas")
                    }

                    actualizarEstadisticas()

                    // Vuelve a Success después de 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = HomeUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Error al actualizar noticias"
                    )

                    // Vuelve a Success después de 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = if (noticias.value.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success
                    }
                }
            )
        }
    }

    /**
     * Selecciona una categoría para filtrar
     */
    fun seleccionarCategoria(categoria: String?) {
        _categoriaSeleccionada.value = categoria
    }

    /**
     * Genera un resumen con IA para una noticia
     */
    fun generarResumen(noticia: DocumentoCTI) {
        viewModelScope.launch {
            generarResumenUseCase(noticia).fold(
                onSuccess = { resumen ->
                    _uiState.value = HomeUiState.ResumenGenerado

                    kotlinx.coroutines.delay(2000)
                    _uiState.value = HomeUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Error al generar resumen"
                    )

                    kotlinx.coroutines.delay(3000)
                    _uiState.value = HomeUiState.Success
                }
            )
        }
    }

    /**
     * Guarda una noticia como favorita
     */
    fun guardarNoticia(noticia: DocumentoCTI) {
        viewModelScope.launch {
            guardarNoticiaUseCase(noticia).fold(
                onSuccess = {
                    _uiState.value = HomeUiState.NoticiaGuardada

                    kotlinx.coroutines.delay(2000)
                    _uiState.value = HomeUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Error al guardar noticia"
                    )

                    kotlinx.coroutines.delay(3000)
                    _uiState.value = HomeUiState.Success
                }
            )
        }
    }

    /**
     * Marca una noticia como consultada
     */
    fun marcarComoConsultada(noticiaId: String) {
        viewModelScope.launch {
            noticiasRepository.marcarComoConsultada(noticiaId)
        }
    }

    /**
     * Actualiza las estadísticas
     */
    private fun actualizarEstadisticas() {
        viewModelScope.launch {
            _estadisticas.value = obtenerEstadisticasUseCase()
        }
    }

    /**
     * Resetea el estado de error
     */
    fun resetearEstado() {
        _uiState.value = HomeUiState.Success
    }
}

// ===== ESTADOS DE LA UI =====

/**
 * Estados de la UI de Home
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    object Empty : HomeUiState()
    object Actualizando : HomeUiState()
    data class ActualizacionExitosa(val mensaje: String) : HomeUiState()
    object ResumenGenerado : HomeUiState()
    object NoticiaGuardada : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}

// ==========================================
// FIN DE ARCHIVO HomeViewModel.kt
// ==========================================