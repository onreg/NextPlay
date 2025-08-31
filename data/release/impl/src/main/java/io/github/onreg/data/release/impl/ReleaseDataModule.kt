package io.github.onreg.data.release.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.data.release.api.ReleaseRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseDataModule {
    @Binds
    @Singleton
    abstract fun bindReleaseRepository(impl: ReleaseRepositoryImpl): ReleaseRepository
}