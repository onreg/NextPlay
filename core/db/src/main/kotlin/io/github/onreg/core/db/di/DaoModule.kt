package io.github.onreg.core.db.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameListDao
import io.github.onreg.core.db.game.dao.GameListRemoteKeysDao
import io.github.onreg.core.db.movies.dao.MovieDao
import io.github.onreg.core.db.movies.dao.MovieRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.ScreenshotDao
import io.github.onreg.core.db.screenshots.dao.ScreenshotRemoteKeysDao
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object DaoModule {
    @Provides
    @Singleton
    public fun provideGameDao(database: NextPlayDatabase): GameDao = database.gameDao()

    @Provides
    @Singleton
    public fun provideGameDetailsDao(database: NextPlayDatabase): GameDetailsDao =
        database.gameDetailsDao()

    @Provides
    @Singleton
    public fun provideGameListDao(database: NextPlayDatabase): GameListDao = database.gameListDao()

    @Provides
    @Singleton
    public fun provideGameListRemoteKeysDao(database: NextPlayDatabase): GameListRemoteKeysDao =
        database.gameListRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideMovieDao(database: NextPlayDatabase): MovieDao = database.movieDao()

    @Provides
    @Singleton
    public fun provideMovieRemoteKeysDao(database: NextPlayDatabase): MovieRemoteKeysDao =
        database.movieRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideScreenshotDao(database: NextPlayDatabase): ScreenshotDao =
        database.screenshotDao()

    @Provides
    @Singleton
    public fun provideScreenshotRemoteKeysDao(database: NextPlayDatabase): ScreenshotRemoteKeysDao =
        database.screenshotRemoteKeysDao()

    @Provides
    @Singleton
    public fun provideSeriesDao(database: NextPlayDatabase): SeriesDao = database.seriesDao()

    @Provides
    @Singleton
    public fun provideSeriesRemoteKeysDao(database: NextPlayDatabase): SeriesRemoteKeysDao =
        database.seriesRemoteKeysDao()
}
