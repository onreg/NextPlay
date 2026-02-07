package io.github.onreg.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.onreg.core.db.common.converter.InstantTypeConverter
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.game.list.dao.GameListDao
import io.github.onreg.core.db.game.list.dao.GameListRemoteKeysDao
import io.github.onreg.core.db.game.list.entity.GameListEntity
import io.github.onreg.core.db.game.list.entity.GameListRemoteKeysEntity
import io.github.onreg.core.db.movies.dao.GameMovieRemoteKeysDao
import io.github.onreg.core.db.movies.dao.GameMoviesDao
import io.github.onreg.core.db.movies.entity.GameMovieCrossRef
import io.github.onreg.core.db.movies.entity.GameMovieRemoteKeysEntity
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.db.platform.dao.PlatformDao
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.screenshots.dao.GameScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.dao.GameScreenshotsDao
import io.github.onreg.core.db.screenshots.entity.GameScreenshotCrossRef
import io.github.onreg.core.db.screenshots.entity.GameScreenshotRemoteKeysEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity

@Database(
    entities = [
        GameEntity::class,
        GameListEntity::class,
        GameListRemoteKeysEntity::class,
        GameDetailsEntity::class,
        ScreenshotEntity::class,
        GameScreenshotCrossRef::class,
        GameScreenshotRemoteKeysEntity::class,
        MovieEntity::class,
        GameMovieCrossRef::class,
        GameMovieRemoteKeysEntity::class,
        SeriesEntity::class,
        SeriesRemoteKeysEntity::class,
        PlatformEntity::class,
        GamePlatformCrossRef::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(InstantTypeConverter::class)
public abstract class NextPlayDatabase : RoomDatabase() {
    public abstract fun gameListDao(): GameListDao

    public abstract fun gameListRemoteKeysDao(): GameListRemoteKeysDao

    public abstract fun gameDetailsDao(): GameDetailsDao

    public abstract fun gameScreenshotsDao(): GameScreenshotsDao

    public abstract fun gameScreenshotRemoteKeysDao(): GameScreenshotRemoteKeysDao

    public abstract fun gameMoviesDao(): GameMoviesDao

    public abstract fun gameMovieRemoteKeysDao(): GameMovieRemoteKeysDao

    public abstract fun seriesDao(): SeriesDao

    public abstract fun seriesRemoteKeysDao(): SeriesRemoteKeysDao

    public abstract fun platformDao(): PlatformDao
}
