package org.kth.countryguesser.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.kth.countryguesser.data.local.room.AppDatabase
import org.kth.countryguesser.data.local.room.LastGuessedDailyDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "country_guesser.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLastGuessedDailyDao(
        database: AppDatabase
    ): LastGuessedDailyDao {
        return database.lastGuessedDailyDao()
    }
}
