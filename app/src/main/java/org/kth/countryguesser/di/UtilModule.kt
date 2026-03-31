package org.kth.countryguesser.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kth.countryguesser.util.WikiDataParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides
    @Singleton
    fun provideWikiDataParser(): WikiDataParser = WikiDataParser()
}

