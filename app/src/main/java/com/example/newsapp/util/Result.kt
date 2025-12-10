package com.example.newsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application principal
 * @HiltAndroidApp inicializa Hilt para inyección de dependencias
 */
@HiltAndroidApp
class NewsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializaciones globales si son necesarias
        // Por ejemplo: configurar Timber para logs, etc.
    }
}