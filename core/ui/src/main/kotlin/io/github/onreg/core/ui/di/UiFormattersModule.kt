package io.github.onreg.core.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.ui.format.InstantTextFormatter
import io.github.onreg.core.ui.format.InstantTextFormatterImpl
import io.github.onreg.core.ui.format.NumberTextFormatter
import io.github.onreg.core.ui.format.NumberTextFormatterImpl

@Module
@InstallIn(SingletonComponent::class)
public abstract class UiFormattersModule {
    @Binds
    public abstract fun bindInstantTextFormatter(impl: InstantTextFormatterImpl): InstantTextFormatter

    @Binds
    public abstract fun bindNumberTextFormatter(impl: NumberTextFormatterImpl): NumberTextFormatter
}
