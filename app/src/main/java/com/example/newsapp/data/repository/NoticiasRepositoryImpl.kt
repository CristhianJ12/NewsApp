package com.example.newsapp.data.repository

import com.example.newsapp.data.local.database.dao.NoticiasDao
import com.example.newsapp.data.local.database.dao.NoticiasGuardadasDao
import com.example.newsapp.data.local.database.toDomain
import com.example.newsapp.data.local.database.toEntity
import com.example.newsapp.data.local.database.toGuardadaEntity
import com.example.newsapp.data.local.preferences.UserPreferencesManager
import com.example.newsapp.data.remote.sources.RSSSource
import com.example.newsapp.domain.model.DocumentoCTI
import com.example.newsapp.domain.repository.EstadisticasNoticias
import com.example.newsapp.domain.repository.NoticiasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticiasRepositoryImpl @Inject constructor(
    private val noticiasDao: NoticiasDao,
    private val noticiasGuardadasDao: NoticiasGuardadasDao,
    private val rssSource: RSSSource,
    private val preferencesManager: UserPreferencesManager
) : NoticiasRepository {

    override fun observarNoticias(): Flow<List<DocumentoCTI>> {
        return noticiasDao.observarTodasLasNoticias().map { it.toDomain() }
    }

    override fun observarPorCategoria(categoria: String): Flow<List<DocumentoCTI>> {
        return noticiasDao.observarPorCategoria(categoria).map { it.toDomain() }
    }

    override fun observarGuardadas(): Flow<List<DocumentoCTI>> {
        return noticiasGuardadasDao.observarGuardadas().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun buscarPorTexto(texto: String, limite: Int): List<DocumentoCTI> {
        return noticiasDao.buscarPorTexto(texto, limite).toDomain()
    }

    override suspend fun obtenerRecientes(horas: Int): List<DocumentoCTI> {
        val timestamp = System.currentTimeMillis() - (horas * 60 * 60 * 1000)
        return noticiasDao.obtenerRecientes(timestamp).toDomain()
    }

    override suspend fun obtenerPorId(id: String): DocumentoCTI? {
        return noticiasDao.obtenerPorId(id)?.toDomain()
    }

    override suspend fun buscarPorKeyword(keyword: String): List<DocumentoCTI> {
        return noticiasDao.buscarPorTexto(keyword).toDomain()
    }

    override suspend fun actualizarNoticias(): Result<Int> {
        return try {
            val resultado = rssSource.obtenerNoticias()

            resultado.fold(
                onSuccess = { noticias ->
                    val entities = noticias.map { it.toEntity() }
                    noticiasDao.insertarTodas(entities)
                    preferencesManager.actualizarTimestamp()
                    Result.success(noticias.size)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar noticias: ${e.message}", e))
        }
    }

    override suspend fun insertarNoticias(noticias: List<DocumentoCTI>): Result<Unit> {
        return try {
            val entities = noticias.map { it.toEntity() }
            noticiasDao.insertarTodas(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al insertar noticias: ${e.message}", e))
        }
    }

    override suspend fun marcarComoConsultada(id: String): Result<Unit> {
        return try {
            noticiasDao.marcarComoConsultado(id, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al marcar noticia: ${e.message}", e))
        }
    }

    override suspend fun guardarNoticia(noticia: DocumentoCTI): Result<Unit> {
        return try {
            val guardada = noticia.toGuardadaEntity()
            noticiasGuardadasDao.guardar(guardada)
            noticiasDao.actualizarGuardado(noticia.id, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar noticia: ${e.message}", e))
        }
    }

    override suspend fun eliminarGuardada(id: String): Result<Unit> {
        return try {
            noticiasGuardadasDao.eliminarPorId(id)
            noticiasDao.actualizarGuardado(id, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar guardada: ${e.message}", e))
        }
    }

    override suspend fun actualizarResumen(id: String, resumen: String): Result<Unit> {
        return try {
            noticiasDao.actualizarResumen(id, resumen)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar resumen: ${e.message}", e))
        }
    }

    override suspend fun limpiarNoticiasAntiguas(): Result<Int> {
        return try {
            val hace24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val eliminadas = noticiasDao.eliminarAnteriores(hace24h)
            Result.success(eliminadas)
        } catch (e: Exception) {
            Result.failure(Exception("Error al limpiar noticias: ${e.message}", e))
        }
    }

    override suspend fun obtenerEstadisticas(): EstadisticasNoticias {
        return try {
            val total = noticiasDao.contarNoticias()
            val hace24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val recientes = noticiasDao.obtenerRecientes(hace24h).size
            val guardadas = noticiasGuardadasDao.contarGuardadas()

            EstadisticasNoticias(
                totalNoticias = total,
                noticiasRecientes = recientes,
                noticiasGuardadas = guardadas
            )
        } catch (e: Exception) {
            EstadisticasNoticias()
        }
    }
}