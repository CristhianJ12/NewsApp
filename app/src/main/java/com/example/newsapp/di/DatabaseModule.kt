package com.example.newsapp.di

import android.content.Context
import androidx.room.Room
import com.example.newsapp.data.local.database.NewsDatabase
import com.example.newsapp.data.local.database.dao.ConfiguracionDao
import com.example.newsapp.data.local.database.dao.NoticiasDao
import com.example.newsapp.data.local.database.dao.NoticiasGuardadasDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la base de datos
 * Proporciona la instancia de Room y los DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Proporciona la instancia de la base de datos
     */
    @Provides
    @Singleton
    fun provideNewsDatabase(
        @ApplicationContext context: Context
    ): NewsDatabase {
        return Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            NewsDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Para desarrollo, eliminar en producción
            .build()
    }

    /**
     * Proporciona el DAO de noticias
     */
    @Provides
    @Singleton
    fun provideNoticiasDao(database: NewsDatabase): NoticiasDao {
        return database.noticiasDao()
    }

    /**
     * Proporciona el DAO de noticias guardadas
     */
    @Provides
    @Singleton
    fun provideNoticiasGuardadasDao(database: NewsDatabase): NoticiasGuardadasDao {
        return database.noticiasGuardadasDao()
    }

    /**
     * Proporciona el DAO de configuración
     */
    @Provides
    @Singleton
    fun provideConfiguracionDao(database: NewsDatabase): ConfiguracionDao {
        return database.configuracionDao()
    }
}