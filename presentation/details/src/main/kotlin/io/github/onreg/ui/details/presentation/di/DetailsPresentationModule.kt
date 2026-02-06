package io.github.onreg.ui.details.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.ui.details.presentation.mapper.GameDetailsUiMapper
import io.github.onreg.ui.details.presentation.mapper.GameDetailsUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
public abstract class DetailsPresentationModule {
    @Binds
    public abstract fun bindGameDetailsUiMapper(impl: GameDetailsUiMapperImpl): GameDetailsUiMapper
}
