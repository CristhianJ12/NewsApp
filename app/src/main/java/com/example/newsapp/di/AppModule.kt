package com.example.newsapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.newsapp.data.local.preferences.UserPreferencesManager
import com.example.newsapp.data.remote.ai.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extension para DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Módulo principal de la aplicación
 * Proporciona servicios generales como IA, DataStore, etc.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proporciona DataStore de preferencias
     */
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Proporciona el manager de preferencias de usuario
     */
    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        dataStore: DataStore<Preferences>
    ): UserPreferencesManager {
        return UserPreferencesManager(dataStore)
    }

    /**
     * Proporciona el servicio de Gemini AI
     *
     * ⚠️ TEMPORAL: API key hardcodeada solo para pruebas
     * ⚠️ PRODUCCIÓN: Eliminar esto y configurar desde la UI
     */
    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        // 🔑 REEMPLAZA ESTO CON TU API KEY COMPLETA
        val apiKey = "AI"  // ← Pega tu API key aquí

        return GeminiService(apiKey = apiKey)
    }

    /**
     * Proporciona el manager de TTS
     */
    @Provides
    @Singleton
    fun provideTTSManager(
        @ApplicationContext context: Context
    ): com.example.newsapp.presentation.voice.TTSManager {
        return com.example.newsapp.presentation.voice.TTSManager(context)
    }

    /**
     * Proporciona el manager de Speech Recognition
     */
    @Provides
    @Singleton
    fun provideSpeechRecognizerManager(
        @ApplicationContext context: Context
    ): com.example.newsapp.presentation.voice.SpeechRecognizerManager {
        return com.example.newsapp.presentation.voice.SpeechRecognizerManager(context)
    }
}
