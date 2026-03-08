package com.marymar.mobile.di

import com.marymar.mobile.data.repository.AuthRepositoryImpl
import com.marymar.mobile.data.repository.OrderRepositoryImpl
import com.marymar.mobile.data.repository.ProductRepositoryImpl
import com.marymar.mobile.domain.repository.AuthRepository
import com.marymar.mobile.domain.repository.OrderRepository
import com.marymar.mobile.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}
