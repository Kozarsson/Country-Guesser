package org.kth.countryguesser.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSourceImpl
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSourceImpl
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.CountryRepositoryImpl
import org.kth.countryguesser.data.repository.FirebaseAuthRepository
import org.kth.countryguesser.data.repository.FirebaseAuthRepositoryImpl
import org.kth.countryguesser.data.repository.FirestoreRepository
import org.kth.countryguesser.data.repository.FirestoreRepositoryImpl
import org.kth.countryguesser.data.repository.LastDailyRepository
import org.kth.countryguesser.data.repository.LastDailyRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindFirestoreRemoteDataSource(
        impl: FirestoreRemoteDataSourceImpl
    ): FirestoreRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCountryRepository(
        impl: CountryRepositoryImpl
    ): CountryRepository

    @Binds
    @Singleton
    abstract fun bindFirebaseAuthRepository(
        impl: FirebaseAuthRepositoryImpl
    ): FirebaseAuthRepository

    @Binds
    @Singleton
    abstract fun bindFirestoreRepository(
        impl: FirestoreRepositoryImpl
    ): FirestoreRepository

    @Binds
    @Singleton
    abstract fun bindLastDailyRepository(
        impl: LastDailyRepositoryImpl
    ): LastDailyRepository
}
