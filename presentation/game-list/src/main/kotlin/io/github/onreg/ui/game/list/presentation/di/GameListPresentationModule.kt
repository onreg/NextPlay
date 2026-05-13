package io.github.onreg.ui.game.list.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapper
import io.github.onreg.ui.game.list.presentation.mapper.GameCardUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
public abstract class GameListPresentationModule {
    @Binds
    public abstract fun bindGameCardUiMapper(impl: GameCardUiMapperImpl): GameCardUiMapper
}
