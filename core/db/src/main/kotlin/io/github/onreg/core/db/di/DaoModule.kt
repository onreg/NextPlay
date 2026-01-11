package io.github.onreg.core.db.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameRemoteKeysDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object DaoModule {
    @Provides
    @Singleton
    public fun provideGameDao(database: NextPlayDatabase): GameDao = database.gameDao()

    @Provides
    @Singleton
    public fun provideGameRemoteKeysDao(database: NextPlayDatabase): GameRemoteKeysDao =
        database.gameRemoteKeysDao()
}