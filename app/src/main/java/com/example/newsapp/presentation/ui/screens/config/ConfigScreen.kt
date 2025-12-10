// ==========================================
// 📄 ARCHIVO: ConfigScreen.kt
// 📁 UBICACIÓN: presentation/ui/screens/config/
// 🎨 TIPO: Composable Screen
// ==========================================

package com.example.newsapp.presentation.ui.screens.config

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla de configuración
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val velocidadVoz by viewModel.velocidadVoz.collectAsState()
    val modoVozActivo by viewModel.modoVozActivo.collectAsState()

    var mostrarDialogoApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner de estado
            AnimatedVisibility(
                visible = uiState !is ConfigUiState.Idle,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EstadoBanner(uiState, onDismiss = { viewModel.resetearEstado() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección: API de Gemini
                SeccionHeader(
                    titulo = "API de Gemini",
                    icono = Icons.Default.Key
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (geminiApiKey.isNotBlank())
                                "API Key configurada ✓"
                            else
                                "API Key no configurada",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (geminiApiKey.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = "Necesitas una API key de Gemini para usar el asistente conversacional",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { mostrarDialogoApiKey = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Configurar API Key")
                        }
                    }
                }

                // Sección: Voz
                SeccionHeader(
                    titulo = "Configuración de Voz",
                    icono = Icons.Default.VolumeUp
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Activar/desactivar voz
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo voz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Habilita comandos de voz y lectura automática",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = modoVozActivo,
                                onCheckedChange = { viewModel.setModoVozActivo(it) }
                            )
                        }

                        Divider()

                        // Velocidad de voz
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Velocidad de voz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(velocidadVoz * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = velocidadVoz,
                                onValueChange = { viewModel.setVelocidadVoz(it) },
                                valueRange = 0.5f..2.0f,
                                steps = 5
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lento", style = MaterialTheme.typography.labelSmall)
                                Text("Rápido", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Sección: Estadísticas
                SeccionHeader(
                    titulo = "Estadísticas",
                    icono = Icons.Default.BarChart
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EstadisticaItem(
                            icono = Icons.Default.Article,
                            titulo = "Noticias totales",
                            valor = estadisticas.totalNoticias.toString()
                        )

                        EstadisticaItem(
                            icono = Icons.Default.NewReleases,
                            titulo = "Noticias recientes (24h)",
                            valor = estadisticas.noticiasRecientes.toString()
                        )

                        EstadisticaItem(
                            icono = Icons.Default.Favorite,
                            titulo = "Noticias guardadas",
                            valor = estadisticas.noticiasGuardadas.toString()
                        )
                    }
                }

                // Sección: Mantenimiento
                SeccionHeader(
                    titulo = "Mantenimiento",
                    icono = Icons.Default.Build
                )

                OutlinedButton(
                    onClick = { viewModel.limpiarNoticiasAntiguas() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Limpiar noticias antiguas (>24h)")
                }

                // Información de la app
                Spacer(Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Asistente de Noticias v1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Powered by Gemini AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Diálogo para configurar API Key
    if (mostrarDialogoApiKey) {
        DialogoApiKey(
            apiKeyActual = geminiApiKey,
            onDismiss = { mostrarDialogoApiKey = false },
            onConfirm = { nuevaKey ->
                viewModel.guardarGeminiApiKey(nuevaKey)
                mostrarDialogoApiKey = false
            }
        )
    }
}

/**
 * Banner de estado
 */
@Composable
private fun EstadoBanner(
    uiState: ConfigUiState,
    onDismiss: () -> Unit
) {
    val (color, icon, text) = when (uiState) {
        is ConfigUiState.ApiKeyGuardada -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Check,
            "API Key guardada correctamente"
        )
        is ConfigUiState.ConfiguracionGuardada -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Check,
            uiState.mensaje
        )
        is ConfigUiState.Limpiando -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.Refresh,
            "Limpiando noticias antiguas..."
        )
        is ConfigUiState.LimpiezaCompletada -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Check,
            uiState.mensaje
        )
        is ConfigUiState.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.Error,
            uiState.mensaje
        )
        else -> return
    }

    Surface(
        color = color,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (uiState !is ConfigUiState.Limpiando) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Header de sección
 */
@Composable
private fun SeccionHeader(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Item de estadística
 */
@Composable
private fun EstadisticaItem(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Diálogo para configurar API Key
 */
@Composable
private fun DialogoApiKey(
    apiKeyActual: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(apiKeyActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Gemini API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa tu API key de Google Gemini:")

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Obtén tu API key gratis en: ai.google.dev",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// FIN DE ARCHIVO ConfigScreen.kt
// ==========================================