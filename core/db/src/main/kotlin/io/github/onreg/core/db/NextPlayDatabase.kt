package io.github.onreg.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.onreg.core.db.common.converter.InstantTypeConverter
import io.github.onreg.core.db.details.dao.GameDetailsDao
import io.github.onreg.core.db.details.entity.DeveloperEntity
import io.github.onreg.core.db.details.entity.GameDetailsEntity
import io.github.onreg.core.db.details.entity.GameDeveloperCrossRef
import io.github.onreg.core.db.game.dao.GameDao
import io.github.onreg.core.db.game.dao.GameListDao
import io.github.onreg.core.db.game.dao.GameListRemoteKeysDao
import io.github.onreg.core.db.game.entity.GameEntity
import io.github.onreg.core.db.game.entity.GameListEntity
import io.github.onreg.core.db.game.entity.GameListRemoteKeysEntity
import io.github.onreg.core.db.game.entity.GamePlatformCrossRef
import io.github.onreg.core.db.movies.dao.MovieDao
import io.github.onreg.core.db.movies.dao.MovieRemoteKeysDao
import io.github.onreg.core.db.movies.entity.MovieEntity
import io.github.onreg.core.db.movies.entity.MovieRemoteKeysEntity
import io.github.onreg.core.db.platform.dao.PlatformDao
import io.github.onreg.core.db.platform.entity.PlatformEntity
import io.github.onreg.core.db.screenshots.dao.ScreenshotDao
import io.github.onreg.core.db.screenshots.dao.ScreenshotRemoteKeysDao
import io.github.onreg.core.db.screenshots.entity.ScreenshotEntity
import io.github.onreg.core.db.screenshots.entity.ScreenshotRemoteKeysEntity
import io.github.onreg.core.db.series.dao.SeriesDao
import io.github.onreg.core.db.series.dao.SeriesRemoteKeysDao
import io.github.onreg.core.db.series.entity.SeriesGameEntity
import io.github.onreg.core.db.series.entity.SeriesRemoteKeysEntity

@Database(
    entities = [
        GameDetailsEntity::class,
        DeveloperEntity::class,
        GameDeveloperCrossRef::class,
        GameEntity::class,
        GameListEntity::class,
        GameListRemoteKeysEntity::class,
        PlatformEntity::class,
        GamePlatformCrossRef::class,
        ScreenshotEntity::class,
        ScreenshotRemoteKeysEntity::class,
        MovieEntity::class,
        MovieRemoteKeysEntity::class,
        SeriesGameEntity::class,
        SeriesRemoteKeysEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantTypeConverter::class)
public abstract class NextPlayDatabase : RoomDatabase() {
    public abstract fun gameDao(): GameDao

    public abstract fun gameDetailsDao(): GameDetailsDao

    public abstract fun gameListDao(): GameListDao

    public abstract fun gameListRemoteKeysDao(): GameListRemoteKeysDao

    public abstract fun platformDao(): PlatformDao

    public abstract fun movieDao(): MovieDao

    public abstract fun movieRemoteKeysDao(): MovieRemoteKeysDao

    public abstract fun screenshotDao(): ScreenshotDao

    public abstract fun screenshotRemoteKeysDao(): ScreenshotRemoteKeysDao

    public abstract fun seriesDao(): SeriesDao

    public abstract fun seriesRemoteKeysDao(): SeriesRemoteKeysDao
}
