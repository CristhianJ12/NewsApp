// ==========================================
// 📄 ARCHIVO: ChatScreen.kt
// 📁 UBICACIÓN: presentation/ui/screens/chat/
// 🎨 TIPO: Composable Screen
// ==========================================

package com.example.newsapp.presentation.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.domain.model.MensajeChat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal de chat conversacional
 * Aquí el usuario interactúa por voz con el asistente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToConfig: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val mensajes by viewModel.mensajes.collectAsState()
    val estadoVoz by viewModel.estadoVoz.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll al último mensaje
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Asistente de Noticias")
                        AnimatedVisibility(visible = estadoVoz is EstadoVoz.Escuchando) {
                            Text(
                                text = "Escuchando...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Botón para limpiar historial
                    IconButton(onClick = { viewModel.limpiarHistorial() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar historial"
                        )
                    }

                    // Botón para ir a Home
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Inicio"
                        )
                    }

                    // Botón para configuración
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón de detener voz (si está hablando)
                AnimatedVisibility(visible = estadoVoz is EstadoVoz.Hablando) {
                    FloatingActionButton(
                        onClick = { viewModel.detenerVoz() },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Detener voz"
                        )
                    }
                }

                // Botón principal de voz
                FloatingActionButton(
                    onClick = {
                        when (estadoVoz) {
                            is EstadoVoz.Escuchando -> viewModel.cancelarReconocimientoVoz()
                            else -> viewModel.iniciarReconocimientoVoz()
                        }
                    },
                    containerColor = when (estadoVoz) {
                        is EstadoVoz.Escuchando -> MaterialTheme.colorScheme.error
                        is EstadoVoz.Hablando -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Icon(
                        imageVector = when (estadoVoz) {
                            is EstadoVoz.Escuchando -> Icons.Default.MicOff
                            is EstadoVoz.Hablando -> Icons.Default.VolumeUp
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Comando de voz"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de estado de voz
            AnimatedVisibility(
                visible = estadoVoz !is EstadoVoz.Inactivo,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EstadoVozBanner(estadoVoz)
            }

            // Mensaje de estado de UI
            AnimatedVisibility(
                visible = uiState !is ChatUiState.Idle && uiState !is ChatUiState.Success,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EstadoUIBanner(uiState)
            }

            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = mensajes,
                    key = { it.id }
                ) { mensaje ->
                    MensajeBubble(mensaje = mensaje)
                }
            }
        }
    }
}

/**
 * Banner que muestra el estado de voz
 */
@Composable
private fun EstadoVozBanner(estadoVoz: EstadoVoz) {
    Surface(
        color = when (estadoVoz) {
            is EstadoVoz.Escuchando, is EstadoVoz.EscuchandoParcial ->
                MaterialTheme.colorScheme.primaryContainer
            is EstadoVoz.Hablando ->
                MaterialTheme.colorScheme.tertiaryContainer
            else ->
                MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (estadoVoz) {
                    is EstadoVoz.Escuchando, is EstadoVoz.EscuchandoParcial ->
                        Icons.Default.Mic
                    is EstadoVoz.Hablando ->
                        Icons.Default.VolumeUp
                    else ->
                        Icons.Default.Refresh
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (estadoVoz) {
                        is EstadoVoz.Iniciando -> "Iniciando..."
                        is EstadoVoz.Escuchando -> "Te estoy escuchando..."
                        is EstadoVoz.EscuchandoParcial -> "Escuchando..."
                        is EstadoVoz.Procesando -> "Procesando..."
                        is EstadoVoz.Hablando -> "Hablando..."
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                if (estadoVoz is EstadoVoz.EscuchandoParcial) {
                    Text(
                        text = estadoVoz.textoParcial,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Banner que muestra el estado de la UI
 */
@Composable
private fun EstadoUIBanner(uiState: ChatUiState) {
    val (color, icon, text) = when (uiState) {
        is ChatUiState.Loading -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.Refresh,
            "Procesando tu consulta..."
        )
        is ChatUiState.ConfiguracionExitosa -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Check,
            "Configuración guardada"
        )
        is ChatUiState.SinResultados -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            Icons.Default.Info,
            "No encontré información sobre eso"
        )
        is ChatUiState.Error -> Triple(
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Burbuja de mensaje individual
 */
@Composable
private fun MensajeBubble(mensaje: MensajeChat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esUsuario)
            Arrangement.End
        else
            Arrangement.Start
    ) {
        if (!mensaje.esUsuario) {
            // Avatar del asistente
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (mensaje.esUsuario) 16.dp else 4.dp,
                    bottomEnd = if (mensaje.esUsuario) 4.dp else 16.dp
                ),
                color = if (mensaje.esUsuario)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                Text(
                    text = mensaje.texto,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mensaje.esUsuario)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // Timestamp
            Text(
                text = formatearHora(mensaje.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )
        }

        if (mensaje.esUsuario) {
            Spacer(modifier = Modifier.width(8.dp))

            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Formatea el timestamp a hora legible
 */
private fun formatearHora(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ==========================================
// FIN DE ARCHIVO ChatScreen.kt
// ==========================================