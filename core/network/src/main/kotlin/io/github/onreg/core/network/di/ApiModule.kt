package io.github.onreg.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.network.rawg.api.GameApi
import io.github.onreg.core.network.rawg.api.GameDetailsApi
import io.github.onreg.core.network.rawg.api.GameMoviesApi
import io.github.onreg.core.network.rawg.api.GameScreenshotsApi
import io.github.onreg.core.network.rawg.api.GameSeriesApi
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
public object ApiModule {
    @Provides
    @Singleton
    public fun providesGameApi(retrofit: Retrofit): GameApi = retrofit.create(GameApi::class.java)

    @Provides
    @Singleton
    public fun providesGameDetailsApi(retrofit: Retrofit): GameDetailsApi =
        retrofit.create(GameDetailsApi::class.java)

    @Provides
    @Singleton
    public fun providesGameScreenshotsApi(retrofit: Retrofit): GameScreenshotsApi =
        retrofit.create(GameScreenshotsApi::class.java)

    @Provides
    @Singleton
    public fun providesGameMoviesApi(retrofit: Retrofit): GameMoviesApi =
        retrofit.create(GameMoviesApi::class.java)

    @Provides
    @Singleton
    public fun providesGameSeriesApi(retrofit: Retrofit): GameSeriesApi =
        retrofit.create(GameSeriesApi::class.java)
}
