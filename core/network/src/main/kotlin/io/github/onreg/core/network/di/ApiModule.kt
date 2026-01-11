package io.github.onreg.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.network.rawg.api.GameApi
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
public object ApiModule {
    @Provides
    @Singleton
    public fun providesGameApi(retrofit: Retrofit): GameApi = retrofit.create(GameApi::class.java)
}