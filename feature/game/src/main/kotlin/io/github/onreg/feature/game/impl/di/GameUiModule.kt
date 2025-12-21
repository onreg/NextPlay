package io.github.onreg.feature.game.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.feature.game.impl.mapper.GameUiMapper
import io.github.onreg.feature.game.impl.mapper.GameUiMapperImpl
import io.github.onreg.feature.game.impl.mapper.PlatformUiMapper
import io.github.onreg.feature.game.impl.mapper.PlatformUiMapperImpl
import io.github.onreg.feature.game.impl.mapper.ResourcesProvider
import io.github.onreg.feature.game.impl.mapper.ResourcesProviderImpl

@Module
@InstallIn(ViewModelComponent::class)
public abstract class GameUiModule {
    @Binds
    public abstract fun bindGameUiMapper(impl: GameUiMapperImpl): GameUiMapper

    @Binds
    public abstract fun bindPlatformUiMapper(impl: PlatformUiMapperImpl): PlatformUiMapper

    @Binds
    public abstract fun bindResourcesProvider(impl: ResourcesProviderImpl): ResourcesProvider
}
