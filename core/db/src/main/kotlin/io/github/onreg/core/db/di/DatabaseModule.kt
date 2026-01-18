package io.github.onreg.core.db.di

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.onreg.core.db.NextPlayDatabase
import io.github.onreg.core.db.TransactionProvider
import javax.inject.Singleton

private const val DB_NAME = "next_play.db"

@Module
@InstallIn(SingletonComponent::class)
public object DatabaseModule {
    @Provides
    @Singleton
    public fun provideGameDatabase(
        @ApplicationContext context: Context,
    ): NextPlayDatabase = Room
        .databaseBuilder(context, NextPlayDatabase::class.java, DB_NAME)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    public fun provideTransactionProvider(database: NextPlayDatabase): TransactionProvider =
        object : TransactionProvider {
            override suspend fun <T> run(block: suspend () -> T): T =
                database.withTransaction(block)
        }
}
