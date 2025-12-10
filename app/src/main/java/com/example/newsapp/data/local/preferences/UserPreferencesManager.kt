package com.example.newsapp.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para preferencias de usuario usando DataStore
 * Guarda configuraciones simples que no necesitan estructura compleja
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        private val PRIMERA_VEZ = booleanPreferencesKey("primera_vez")
        private val ULTIMA_ACTUALIZACION = longPreferencesKey("ultima_actualizacion")
        private val MODO_VOZ_ACTIVO = booleanPreferencesKey("modo_voz_activo")
        private val VELOCIDAD_VOZ = floatPreferencesKey("velocidad_voz")
    }

    /**
     * Observa la API key de Gemini
     */
    val geminiApiKey: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[GEMINI_API_KEY] ?: ""
        }

    /**
     * Observa si es primera vez que usa la app
     */
    val esPrimeraVez: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PRIMERA_VEZ] ?: true
        }

    /**
     * Observa la última actualización de noticias
     */
    val ultimaActualizacion: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ULTIMA_ACTUALIZACION] ?: 0L
        }

    /**
     * Observa si el modo voz está activo
     */
    val modoVozActivo: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[MODO_VOZ_ACTIVO] ?: true
        }

    /**
     * Observa la velocidad de voz
     */
    val velocidadVoz: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[VELOCIDAD_VOZ] ?: 1.0f
        }

    /**
     * Guarda la API key de Gemini
     */
    suspend fun setGeminiApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY] = apiKey
        }
    }

    /**
     * Marca que ya no es primera vez
     */
    suspend fun marcarComoUsado() {
        dataStore.edit { preferences ->
            preferences[PRIMERA_VEZ] = false
        }
    }

    /**
     * Actualiza el timestamp de última actualización
     */
    suspend fun actualizarTimestamp() {
        dataStore.edit { preferences ->
            preferences[ULTIMA_ACTUALIZACION] = System.currentTimeMillis()
        }
    }

    /**
     * Activa/desactiva el modo voz
     */
    suspend fun setModoVozActivo(activo: Boolean) {
        dataStore.edit { preferences ->
            preferences[MODO_VOZ_ACTIVO] = activo
        }
    }

    /**
     * Establece la velocidad de voz (0.5 - 2.0)
     */
    suspend fun setVelocidadVoz(velocidad: Float) {
        dataStore.edit { preferences ->
            preferences[VELOCIDAD_VOZ] = velocidad.coerceIn(0.5f, 2.0f)
        }
    }

    /**
     * Limpia todas las preferencias
     */
    suspend fun limpiarPreferencias() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}