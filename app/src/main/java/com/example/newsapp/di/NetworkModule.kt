package com.example.newsapp.di

import com.prof18.rssparser.RssParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo de Hilt para networking
 * Proporciona Retrofit, OkHttp y RSS Parser
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Proporciona el cliente OkHttp con logging
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Proporciona Retrofit (para futuras APIs oficiales)
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Proporciona el RSS Parser
     * La librería RssParser 6.0.8 usa su propio OkHttpClient interno
     */
    @Provides
    @Singleton
    fun provideRssParser(): RssParser {
        return RssParser()
    }

    /**
     * Proporciona la fuente RSS
     */
    @Provides
    @Singleton
    fun provideRSSSource(rssParser: RssParser): com.example.newsapp.data.remote.sources.RSSSource {
        return com.example.newsapp.data.remote.sources.RSSSource(rssParser)
    }
}