// ==========================================
// 📄 ARCHIVO: HomeScreen.kt
// 📁 UBICACIÓN: presentation/ui/screens/home/
// 🎨 TIPO: Composable Screen
// ==========================================

package com.example.newsapp.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.newsapp.domain.model.DocumentoCTI
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal - Lista de noticias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNoticiaClick: (DocumentoCTI) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val noticias by viewModel.noticias.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Noticias del Día")
                        if (estadisticas.totalNoticias > 0) {
                            Text(
                                text = "${estadisticas.totalNoticias} noticias",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    // Botón para ir al chat
                    IconButton(onClick = onNavigateToChat) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat con asistente"
                        )
                    }

                    // Botón de configuración
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
            FloatingActionButton(
                onClick = { viewModel.actualizarNoticias() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar noticias"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner de estado
            AnimatedVisibility(
                visible = uiState !is HomeUiState.Success && uiState !is HomeUiState.Loading,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                EstadoBanner(uiState, onDismiss = { viewModel.resetearEstado() })
            }

            // Filtros de categoría
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = categoriaSeleccionada == null,
                        onClick = { viewModel.seleccionarCategoria(null) },
                        label = { Text("Todas") },
                        leadingIcon = if (categoriaSeleccionada == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }

                items(viewModel.categoriasDisponibles) { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { viewModel.seleccionarCategoria(categoria) },
                        label = { Text(categoria) },
                        leadingIcon = if (categoriaSeleccionada == categoria) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Divider()

            // Contenido principal
            when {
                uiState is HomeUiState.Loading -> {
                    LoadingView()
                }

                uiState is HomeUiState.Empty -> {
                    EmptyView(
                        onActualizar = { viewModel.actualizarNoticias() }
                    )
                }

                noticias.isEmpty() && categoriaSeleccionada != null -> {
                    EmptyView(
                        mensaje = "No hay noticias en esta categoría",
                        onActualizar = { viewModel.seleccionarCategoria(null) },
                        textoBoton = "Ver todas"
                    )
                }

                else -> {
                    NoticiasListView(
                        noticias = noticias,
                        onNoticiaClick = onNoticiaClick,
                        onGenerarResumen = { viewModel.generarResumen(it) },
                        onGuardarNoticia = { viewModel.guardarNoticia(it) }
                    )
                }
            }
        }
    }
}

/**
 * Banner de estado de la UI
 */
@Composable
private fun EstadoBanner(
    uiState: HomeUiState,
    onDismiss: () -> Unit
) {
    val (color, icon, text) = when (uiState) {
        is HomeUiState.Actualizando -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.Refresh,
            "Actualizando noticias..."
        )
        is HomeUiState.ActualizacionExitosa -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Check,
            uiState.mensaje
        )
        is HomeUiState.ResumenGenerado -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.AutoAwesome,
            "Resumen generado con IA"
        )
        is HomeUiState.NoticiaGuardada -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Favorite,
            "Noticia guardada"
        )
        is HomeUiState.Error -> Triple(
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
            if (uiState !is HomeUiState.Actualizando) {
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
 * Vista de carga
 */
@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Cargando noticias...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Vista vacía (sin noticias)
 */
@Composable
private fun EmptyView(
    mensaje: String = "No hay noticias disponibles",
    onActualizar: () -> Unit,
    textoBoton: String = "Actualizar"
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = onActualizar) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(textoBoton)
            }
        }
    }
}

/**
 * Lista de noticias
 */
@Composable
private fun NoticiasListView(
    noticias: List<DocumentoCTI>,
    onNoticiaClick: (DocumentoCTI) -> Unit,
    onGenerarResumen: (DocumentoCTI) -> Unit,
    onGuardarNoticia: (DocumentoCTI) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = noticias,
            key = { it.id }
        ) { noticia ->
            NoticiaCard(
                noticia = noticia,
                onClick = { onNoticiaClick(noticia) },
                onGenerarResumen = { onGenerarResumen(noticia) },
                onGuardar = { onGuardarNoticia(noticia) }
            )
        }
    }
}

/**
 * Card de una noticia
 */
@Composable
private fun NoticiaCard(
    noticia: DocumentoCTI,
    onClick: () -> Unit,
    onGenerarResumen: () -> Unit,
    onGuardar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = noticia.categoria,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = noticia.diario,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Título
            Text(
                text = noticia.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Resumen o contenido
            if (noticia.resumenEjecutivo != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = noticia.resumenEjecutivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Text(
                    text = noticia.contenidoCompleto,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Footer con acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatearFecha(noticia.fechaPublicacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de resumen
                    if (noticia.resumenEjecutivo == null) {
                        FilledTonalIconButton(
                            onClick = onGenerarResumen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generar resumen",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Botón de guardar
                    FilledTonalIconButton(
                        onClick = onGuardar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (noticia.esGuardado)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(18.dp),
                            tint = if (noticia.esGuardado)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formatea fecha a texto legible
 */
private fun formatearFecha(timestamp: Long): String {
    val ahora = System.currentTimeMillis()
    val diferencia = ahora - timestamp

    return when {
        diferencia < 60_000 -> "Hace un momento"
        diferencia < 3600_000 -> "Hace ${diferencia / 60_000} min"
        diferencia < 86400_000 -> "Hace ${diferencia / 3600_000} h"
        else -> {
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "PE"))
            sdf.format(Date(timestamp))
        }
    }
}

// ==========================================
// FIN DE ARCHIVO HomeScreen.kt
// ==========================================