// ==========================================
// 📄 ARCHIVO: MainActivity.kt
// 📁 UBICACIÓN: / (raíz del paquete principal)
// 🟢 TIPO: Activity
// ==========================================

package com.example.newsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.newsapp.presentation.ui.navigation.NavGraph
import com.example.newsapp.presentation.ui.theme.NewsAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal de la aplicación
 *
 * @AndroidEntryPoint permite la inyección de dependencias con Hilt
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Launcher para solicitar permiso de audio
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido - la app puede usar el micrófono
        } else {
            // Permiso denegado - mostrar mensaje al usuario
            // TODO: Mostrar un diálogo explicando por qué necesitamos el permiso
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicita permiso de audio si no está concedido
        solicitarPermisoAudio()

        setContent {
            NewsAppTheme {
                // Surface es el contenedor principal de Material Design
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // NavGraph maneja toda la navegación de la app
                    NavGraph()
                }
            }
        }
    }

    /**
     * Solicita el permiso RECORD_AUDIO si no está concedido
     * Necesario para el reconocimiento de voz
     */
    private fun solicitarPermisoAudio() {
        when {
            // Permiso ya concedido
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // No hacer nada, ya tenemos el permiso
            }

            // Deberíamos mostrar una explicación
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Aquí podrías mostrar un diálogo explicando por qué necesitas el permiso
                // Por ahora, solicitamos directamente
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            // Solicitar el permiso directamente
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

// ==========================================
// FIN DE ARCHIVO MainActivity.kt
// ==========================================