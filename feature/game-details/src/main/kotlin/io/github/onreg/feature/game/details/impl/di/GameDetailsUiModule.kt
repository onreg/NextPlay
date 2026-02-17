package io.github.onreg.feature.game.details.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsStateMapper
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsStateMapperImpl
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsUiMapper
import io.github.onreg.feature.game.details.impl.mapper.GameDetailsUiMapperImpl
import io.github.onreg.feature.game.details.impl.mapper.MovieUiMapper
import io.github.onreg.feature.game.details.impl.mapper.MovieUiMapperImpl
import io.github.onreg.feature.game.details.impl.mapper.ScreenshotUiMapper
import io.github.onreg.feature.game.details.impl.mapper.ScreenshotUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class GameDetailsUiModule {
    @Binds
    abstract fun bindGameDetailsUiMapper(impl: GameDetailsUiMapperImpl): GameDetailsUiMapper

    @Binds
    abstract fun bindGameDetailsStateMapper(
        impl: GameDetailsStateMapperImpl,
    ): GameDetailsStateMapper

    @Binds
    abstract fun bindScreenshotUiMapper(impl: ScreenshotUiMapperImpl): ScreenshotUiMapper

    @Binds
    abstract fun bindMovieUiMapper(impl: MovieUiMapperImpl): MovieUiMapper
}
