package io.github.onreg.core.util.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.util.format.InstantTextFormatter
import io.github.onreg.core.util.format.InstantTextFormatterImpl
import io.github.onreg.core.util.format.NumberTextFormatter
import io.github.onreg.core.util.format.NumberTextFormatterImpl

@Module
@InstallIn(SingletonComponent::class)
public abstract class FormattersModule {
    @Binds
    public abstract fun bindInstantTextFormatter(impl: InstantTextFormatterImpl): InstantTextFormatter

    @Binds
    public abstract fun bindNumberTextFormatter(impl: NumberTextFormatterImpl): NumberTextFormatter
}
