package com.example.newsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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

    // Variable para saber si el permiso fue concedido
    private var permisoAudioConcedido = false

    // Launcher para solicitar permiso de audio
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permisoAudioConcedido = isGranted

        if (isGranted) {
            Toast.makeText(
                this,
                "✅ Permiso de micrófono concedido. Ya puedes usar comandos de voz.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "⚠️ Permiso de micrófono denegado. No podrás usar comandos de voz.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicita permiso de audio al iniciar
        solicitarPermisoAudio()

        setContent {
            NewsAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Verifica permisos cada vez que la app vuelve al frente
        if (!verificarPermisoAudio()) {
            Toast.makeText(
                this,
                "💡 Tip: Necesitas dar permiso de micrófono para usar comandos de voz",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Verifica si el permiso de audio está concedido
     */
    private fun verificarPermisoAudio(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita el permiso RECORD_AUDIO si no está concedido
     */
    private fun solicitarPermisoAudio() {
        when {
            // Permiso ya concedido
            verificarPermisoAudio() -> {
                permisoAudioConcedido = true
            }

            // Deberíamos mostrar una explicación
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    this,
                    "🎤 Esta app necesita acceso al micrófono para reconocer tus comandos de voz",
                    Toast.LENGTH_LONG
                ).show()

                // Solicita después de mostrar el mensaje
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            // Solicitar el permiso directamente
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}