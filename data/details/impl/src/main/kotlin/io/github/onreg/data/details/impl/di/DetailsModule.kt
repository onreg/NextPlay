package io.github.onreg.data.details.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.details.api.GameDetailsRepository
import io.github.onreg.data.details.impl.GameDetailsRepositoryImpl
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsDtoMapperImpl
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapper
import io.github.onreg.data.details.impl.mapper.GameDetailsEntityMapperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class DetailsModule {
    @Binds
    @Singleton
    public abstract fun bindGameDetailsRepository(
        impl: GameDetailsRepositoryImpl,
    ): GameDetailsRepository

    @Binds
    public abstract fun bindGameDetailsDtoMapper(
        impl: GameDetailsDtoMapperImpl,
    ): GameDetailsDtoMapper

    @Binds
    public abstract fun bindGameDetailsEntityMapper(
        impl: GameDetailsEntityMapperImpl,
    ): GameDetailsEntityMapper
}
