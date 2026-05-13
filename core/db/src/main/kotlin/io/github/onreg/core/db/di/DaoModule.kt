package io.github.onreg.core.db.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.company.dao.GameCompanyDao
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.list.dao.GameListRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMovieRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.db.platform.dao.PlatformDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
public object DaoModule {
    @Provides
    @Singleton
    public fun provideGameDao(database: NextPlayDatabase): GameDao = database.gameDao()

    @Provides
    @Singleton
    public fun provideGameListDao(database: NextPlayDatabase): GameListDao = database.gameListDao()

    @Provides
    @Singleton
    public fun provideGameListRemoteKeysDao(database: NextPlayDatabase): GameListRemoteKeysDao =
        database.gameListRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideGameDetailsDao(database: NextPlayDatabase): GameDetailsDao =
        database.gameDetailsDao()

    @Provides
    @Singleton
    public fun provideGameCompanyDao(database: NextPlayDatabase): GameCompanyDao =
        database.gameCompanyDao()

    @Provides
    @Singleton
    public fun provideGameScreenshotsDao(database: NextPlayDatabase): GameScreenshotsDao =
        database.gameScreenshotsDao()

    @Provides
    @Singleton
    public fun provideGameScreenshotRemoteKeysDao(
        database: NextPlayDatabase,
    ): GameScreenshotRemoteKeysDao = database.gameScreenshotRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideGameMoviesDao(database: NextPlayDatabase): GameMoviesDao =
        database.gameMoviesDao()

    @Provides
    @Singleton
    public fun provideGameMovieRemoteKeysDao(database: NextPlayDatabase): GameMovieRemoteKeysDao =
        database.gameMovieRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideSeriesDao(database: NextPlayDatabase): SeriesDao = database.seriesDao()

    @Provides
    @Singleton
    public fun provideSeriesRemoteKeysDao(database: NextPlayDatabase): SeriesRemoteKeysDao =
        database.seriesRemoteKeysDao()

    @Provides
    @Singleton
    public fun providePlatformDao(database: NextPlayDatabase): PlatformDao = database.platformDao()
}
