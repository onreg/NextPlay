package io.github.onreg.ui.game.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.ui.game.presentation.mapper.GameUiMapper
import io.github.onreg.ui.game.presentation.mapper.GameUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
public abstract class GamePresentationModule {
    @Binds
    public abstract fun bindGameUiMapper(impl: GameUiMapperImpl): GameUiMapper
}
