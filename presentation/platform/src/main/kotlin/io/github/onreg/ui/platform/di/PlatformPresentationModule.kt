package io.github.onreg.ui.platform.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.onreg.ui.platform.mapper.PlatformUiMapper
import io.github.onreg.ui.platform.mapper.PlatformUiMapperImpl

@Module
@InstallIn(ViewModelComponent::class)
public abstract class PlatformPresentationModule {
    @Binds
    public abstract fun bindPlatformUiMapper(impl: PlatformUiMapperImpl): PlatformUiMapper
}
