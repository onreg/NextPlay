package io.github.onreg.core.util.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.util.android.resources.ResourcesProvider
import io.github.onreg.core.util.android.resources.ResourcesProviderImpl

@Module
@InstallIn(SingletonComponent::class)
public abstract class AndroidUtilsModule {
    @Binds
    public abstract fun bindResourcesProvider(impl: ResourcesProviderImpl): ResourcesProvider
}
