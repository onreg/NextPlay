package io.github.onreg.feature.game.details.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.feature.game.details.impl.ui.mapper.GameDetailsUiMapper
import io.github.onreg.feature.game.details.impl.ui.mapper.GameDetailsUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class GameDetailsUiModule {
    @Binds
    abstract fun bindGameDetailsUiMapper(impl: GameDetailsUiMapperImpl): GameDetailsUiMapper
}
