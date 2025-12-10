package com.example.newsapp.di

import com.example.newsapp.data.local.database.dao.ConfiguracionDao
import com.example.newsapp.data.local.database.dao.NoticiasDao
import com.example.newsapp.data.local.database.dao.NoticiasGuardadasDao
import com.example.newsapp.data.local.preferences.UserPreferencesManager
import com.example.newsapp.data.remote.sources.RSSSource
import com.example.newsapp.data.repository.ConfiguracionRepositoryImpl
import com.example.newsapp.data.repository.NoticiasRepositoryImpl
import com.example.newsapp.domain.repository.ConfiguracionRepository
import com.example.newsapp.domain.repository.NoticiasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para repositorios
 * Vincula las interfaces con sus implementaciones
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Proporciona el repositorio de noticias
     */
    @Provides
    @Singleton
    fun provideNoticiasRepository(
        noticiasDao: NoticiasDao,
        noticiasGuardadasDao: NoticiasGuardadasDao,
        rssSource: RSSSource,
        preferencesManager: UserPreferencesManager
    ): NoticiasRepository {
        return NoticiasRepositoryImpl(
            noticiasDao = noticiasDao,
            noticiasGuardadasDao = noticiasGuardadasDao,
            rssSource = rssSource,
            preferencesManager = preferencesManager
        )
    }

    /**
     * Proporciona el repositorio de configuración
     */
    @Provides
    @Singleton
    fun provideConfiguracionRepository(
        configuracionDao: ConfiguracionDao
    ): ConfiguracionRepository {
        return ConfiguracionRepositoryImpl(
            configuracionDao = configuracionDao
        )
    }
}