package io.github.onreg.data.game.impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.game.api.GameRepository
import io.github.onreg.data.game.impl.GameRepositoryImpl
import io.github.onreg.data.game.impl.mapper.GameDtoMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapper
import io.github.onreg.data.game.impl.mapper.GameEntityMapperImpl
import io.github.onreg.data.game.impl.mapper.GameDtoMapperImpl
import io.github.onreg.data.game.impl.paging.GamePagingConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class GameModule {

    @Binds
    @Singleton
    public abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    public abstract fun bindGameEntityMapper(impl: GameEntityMapperImpl): GameEntityMapper

    @Binds
    public abstract fun bindGameDtoMapper(impl: GameDtoMapperImpl): GameDtoMapper

    public companion object {
        @Provides
        @Singleton
        public fun providePagingConfig(): GamePagingConfig = GamePagingConfig(
            pageSize = 20,
            prefetchDistance = 2,
            initialLoadSize = 40,
            maxSize = 200
        )
    }
}
