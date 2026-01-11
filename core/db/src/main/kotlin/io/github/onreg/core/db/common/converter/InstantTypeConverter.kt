package io.github.onreg.core.db.common.converter

import androidx.room.TypeConverter
import java.time.Instant

public class InstantTypeConverter {

    @TypeConverter
    public fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    public fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}